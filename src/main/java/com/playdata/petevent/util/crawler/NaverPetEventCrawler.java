package com.playdata.petevent.util.crawler;

import com.playdata.petevent.util.entity.PetEvent;
import com.playdata.petevent.util.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/**
 * 네이버에서 "펫 박람회" 검색 결과를 크롤링하여,
 * 상세 페이지에서 정보를 추출하고 DB에 저장하는 컴포넌트 클래스입니다.
 */
@Component // Spring Bean 등록 (의존성 주입 가능)
@RequiredArgsConstructor // final 필드에 대해 생성자 자동 생성
public class NaverPetEventCrawler {

    private final PetRepository petEventRepository; // DB CRUD 작업을 위한 JPA Repository

    // 이미지 다운로드 저장 경로 (로컬)
    private static final String SAVE_DIR = "/Users/ubing/Desktop/playdata/update/pet/";

    // 크롤링 시작 URL: 네이버 "펫 박람회" 검색 결과 첫 페이지
    private static final String BASE_URL = "https://search.naver.com/search.naver?query=%ED%8E%AB+%EB%B0%95%EB%9E%8C%ED%9A%8C";

    /**
     * 크롤링을 수행하는 메인 메서드입니다.
     * 1. 검색 결과 페이지를 순회하며 상세 페이지 URL을 수집
     * 2. 각 상세 페이지 방문해 박람회 정보 추출 및 DB 저장
     */
    public void crawl() {
        WebDriver driver = new ChromeDriver(); // Selenium ChromeDriver 인스턴스 생성
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 요소 로딩 최대 대기 시간 10초

        // 상세 페이지 URL을 저장하는 Set (중복 제거 및 순서 유지)
        Set<String> detailUrls = collectDetailUrls(driver, wait);

        // 수집한 상세 페이지 URL을 하나씩 방문하며 데이터 처리
        for (String detailUrl : detailUrls) {
            try {
                driver.get(detailUrl); // 상세 페이지 접속
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".sc_new.cs_common_module.case_normal.color_9._cs_festival")));

                // 상세 페이지에서 정보를 추출하여 PetEvent 객체 생성
                PetEvent petEvent = extractDetails(driver);

                // DB에 저장 또는 업데이트 처리
                saveOrUpdateEvent(petEventRepository, petEvent);

            } catch (Exception e) {
                System.out.println("상세 페이지 크롤링 오류: " + detailUrl);
                e.printStackTrace();
            }
        }

        driver.quit(); // 드라이버 종료, 리소스 해제
    }

    /**
     * 검색 결과 페이지에서 상세 페이지 URL을 수집하는 메서드입니다.
     * 페이징을 자동으로 처리하며, 모든 상세 URL을 Set에 담아 반환합니다.
     *
     * @param driver Selenium WebDriver 인스턴스
     * @param wait 명시적 대기용 WebDriverWait 인스턴스
     * @return 중복 없이 순서가 유지된 상세 페이지 URL Set
     */
    private Set<String> collectDetailUrls(WebDriver driver, WebDriverWait wait) {
        Set<String> detailUrls = new LinkedHashSet<>();

        try {
            driver.get(BASE_URL); // 검색 결과 첫 페이지 접속

            // 다음 페이지가 없을 때까지 반복
            while (true) {
                // 박람회 카드 리스트 영역이 완전히 로딩될 때까지 대기
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".sc_new.cs_common_module.case_list.color_5._cs_festival_list")));

                // 상세 페이지 링크를 포함한 각 카드의 이미지 박스 a 태그 선택
                List<WebElement> cards = driver.findElements(By.cssSelector(".card_item a.img_box"));
                for (WebElement card : cards) {
                    String href = card.getAttribute("href"); // href 속성 값 추출
                    if (!href.startsWith("http")) {
                        // 상대경로일 경우 절대경로로 변환
                        href = "https://search.naver.com/search.naver" + href;
                    }
                    detailUrls.add(href); // Set에 저장하여 중복 제거
                }

                // 다음 페이지 버튼 클릭 시도
                try {
                    WebElement nextPage = driver.findElement(By.cssSelector(".pgs .pg_next.on"));
                    nextPage.click(); // 다음 페이지 클릭
                    Thread.sleep(2000); // 페이지 전환 안정성 위해 2초 대기
                } catch (NoSuchElementException e) {
                    // 다음 페이지 버튼이 없으면 페이징 종료
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return detailUrls;
    }

    /**
     * 상세 페이지 내에서 박람회 관련 정보를 추출하는 메서드입니다.
     * Selenium WebDriver가 상세 페이지에 이미 위치해 있다고 가정합니다.
     *
     * @param driver 상세 페이지에서 동작하는 WebDriver
     * @return 추출한 정보를 담은 PetEvent 엔티티 객체
     */
    private PetEvent extractDetails(WebDriver driver) {
        // 필수 항목: 행사 제목과 출처 (항상 존재한다고 가정)
        String eventTitle = safeFindText(driver, By.cssSelector(".title strong._text"));
        String source = safeFindText(driver, By.cssSelector(".title span.state_end"));

        // 행사 공식 URL (없을 수도 있음)
        String eventUrl = safeFindAttribute(driver, By.cssSelector(".title._title_ellipsis a.area_text_title"), "href");

        // 행사 대표 이미지 다운로드 경로
        String imagePath = "";
        try {
            WebElement imageEl = driver.findElement(By.cssSelector(".detail_info img"));
            imagePath = downloadImage(imageEl.getAttribute("src"));
        } catch (NoSuchElementException ignored) {}

        // 기타 상세 정보 변수 초기화
        String eventDate = "", reservationDate = "", eventTime = "", location = "", eventMoney = "";

        // 상세 정보들이 들어있는 여러 info_group 요소 수집
        List<WebElement> infoGroups = driver.findElements(By.cssSelector(".info_group"));
        for (WebElement info : infoGroups) {
            String label = info.getText();

            // '기간' + '~' 문자열 포함 시 기간과 예약기간 추출
            if (label.contains("기간") && label.contains("~")) {
                try {
                    // 날짜 관련 텍스트 추출
                    List<WebElement> dates = info.findElements(By.cssSelector(".text"));
                    if (dates.size() >= 2) {
                        eventDate = dates.get(0).getText() + " " + dates.get(1).getText();
                    }
                    // 숨겨진 예약 기간 텍스트는 JavaScriptExecutor로 직접 읽음
                    WebElement hiddenEl = info.findElement(By.cssSelector(".more_list .text"));
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    reservationDate = (String) js.executeScript("return arguments[0].textContent;", hiddenEl);
                } catch (NoSuchElementException ignored) {
                    reservationDate = "";
                }
            }
            // '시간' 정보 추출
            else if (label.contains("시간")) {
                eventTime = safeFindText(info, By.cssSelector("dd"));
            }
            // '장소' 정보 추출
            else if (label.contains("장소")) {
                location = safeFindText(info, By.cssSelector("dd a"));
            }
            // '요금' 정보 추출
            else if (label.contains("요금")) {
                eventMoney = safeFindText(info, By.cssSelector(".desc._text"));
            }
        }

        // 중복 체크용 MD5 해시 생성 (핵심 필드 조합)
        String hash = generateHash(eventTitle, eventUrl, location);

        // PetEvent 객체 생성 (Builder 패턴 사용)
        return PetEvent.builder()
                .eventTitle(eventTitle)
                .source(source)
                .eventUrl(eventUrl)
                .eventDate(eventDate)
                .reservationDate(reservationDate)
                .eventTime(eventTime)
                .eventMoney(eventMoney)
                .location(location)
                .imagePath(imagePath)
                .hash(hash)
                .build();
    }

    /**
     * DB에 이미 존재하는 데이터는 업데이트하고,
     * 없으면 새로 저장하는 메서드입니다.
     *
     * @param repository PetEvent JPA Repository
     * @param petEvent 저장 또는 업데이트할 PetEvent 객체
     */
    private void saveOrUpdateEvent(PetRepository repository, PetEvent petEvent) {
        // 해시값으로 기존 데이터 조회
        Optional<PetEvent> optionalEvent = repository.findByHash(petEvent.getHash());

        if (optionalEvent.isPresent()) {
            PetEvent existing = optionalEvent.get();
            // 기존 데이터와 비교 후 변경사항이 있을 경우 업데이트
            if (!existing.equals(petEvent)) {
                // 필요한 필드만 업데이트하는 커스텀 메서드 호출 (엔티티 내부 구현 가정)
                existing.update(petEvent.getSource(), petEvent.getEventTitle(), petEvent.getEventUrl(),
                        petEvent.getLocation(), petEvent.getEventDate(), petEvent.getReservationDate(), petEvent.getImagePath());

                repository.save(existing);
            }
        } else {
            // 신규 데이터는 바로 저장
            repository.save(petEvent);
        }
    }

    /**
     * 외부 이미지 URL에서 파일을 다운로드하여
     * 로컬 SAVE_DIR 경로에 저장하고 저장된 경로를 반환합니다.
     *
     * @param imageUrl 이미지 URL
     * @return 저장된 이미지 파일 경로, 실패 시 null
     */
    private String downloadImage(String imageUrl) {
        try {
            // UUID를 사용해 중복 가능성 없는 파일명 생성
            String filename = UUID.randomUUID() + ".png";

            // 저장할 경로 객체 생성
            Path targetPath = Paths.get(SAVE_DIR, filename);

            // URL 스트림 열고 파일로 복사 (덮어쓰기 옵션)
            try (InputStream in = new URL(imageUrl).openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 저장 경로 반환
            return targetPath.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 여러 문자열을 합쳐 MD5 해시를 생성하는 유틸리티 메서드입니다.
     * 중복 방지를 위한 고유 식별자 생성에 사용됩니다.
     *
     * @param values 해시 생성에 사용할 문자열 배열
     * @return MD5 해시 문자열 (16진수)
     */
    private String generateHash(String... values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(Optional.ofNullable(value).orElse("")); // null 처리
        }
        return DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 안전하게 요소의 텍스트를 가져오는 헬퍼 메서드입니다.
     * 요소가 없으면 빈 문자열 반환.
     *
     * @param parent 요소 탐색 시작 위치
     * @param by 찾을 요소의 By 셀렉터
     * @return 요소의 텍스트, 없으면 빈 문자열
     */
    private String safeFindText(SearchContext parent, By by) {
        try {
            return parent.findElement(by).getText();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /**
     * 안전하게 요소의 특정 속성 값을 가져오는 헬퍼 메서드입니다.
     * 요소가 없으면 빈 문자열 반환.
     *
     * @param parent 요소 탐색 시작 위치
     * @param by 찾을 요소의 By 셀렉터
     * @param attr 추출할 속성명 (예: "href", "src")
     * @return 속성값, 없으면 빈 문자열
     */
    private String safeFindAttribute(SearchContext parent, By by, String attr) {
        try {
            return parent.findElement(by).getAttribute(attr);
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}