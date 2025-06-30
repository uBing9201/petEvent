//package com.playdata.petevent.crawler;
//
//import com.playdata.petevent.entity.PetEvent;
//import com.playdata.petevent.repository.PetRepository;
//
//import java.time.Duration;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import java.io.File;
//import java.nio.file.*;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//import lombok.RequiredArgsConstructor;
//import org.openqa.selenium.*;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class PetMoreFairCrawler {
//
//    private static final String SAVE_DIR = "/Users/ubing/Desktop/playdata/update/pet/petandmore/";
//
//    private final PetRepository petRepository;
//
//    /**
//     * 크롤링을 수행하고 결과를 DB에 중복 체크 후 저장 또는 업데이트한다.
//     *
//     * @return 저장 또는 업데이트된 PetEvent 리스트 (현재는 한 건)
//     */
//    public List<PetEvent> crawl() {
//        List<PetEvent> resultList = new ArrayList<>();
//
//        // 크롬 드라이버 실행
//        WebDriver driver = new ChromeDriver();
//
//        try {
//            driver.get("https://ilovepets.co.kr/?main=0");
//
//            // 팝업 창이 있으면 닫기 (최대 5초 대기)
//            try {
//                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
//                WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("b.bw_popup_close")));
//                closeBtn.click();
//            } catch (TimeoutException ignored) {
//                // 팝업이 없으면 그냥 넘어감
//            }
//
//            // 메인 뷰 요소 찾기
//            WebElement mainView = driver.findElement(By.cssSelector(".main_view"));
//
//            // 메인 뷰 스크린샷 저장
//            File screenshot = mainView.getScreenshotAs(OutputType.FILE);
//            Path screenshotPath = Paths.get(SAVE_DIR, "main_view_capture.png");
//            Files.createDirectories(screenshotPath.getParent());
//            Files.copy(screenshot.toPath(), screenshotPath, StandardCopyOption.REPLACE_EXISTING);
//            String capturedImagePath = screenshotPath.toString();
//
//            // 이벤트 제목 추출
//            String eventTitle = mainView.findElement(By.cssSelector(".tb > h3")).getText();
//
//            // .tb .bg 요소 목록 (날짜, 장소 등 정보 포함)
//            List<WebElement> bgElements = mainView.findElements(By.cssSelector(".tb .bg"));
//
//            // 날짜, 장소 정보 추출 및 가공
//            List<Date> startDates = new ArrayList<>();
//            List<Date> endDates = new ArrayList<>();
//            List<String> startDays = new ArrayList<>();
//            List<String> endDays = new ArrayList<>();
//            List<String> locations = new ArrayList<>();
//
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
//
//            // 이벤트 제목에서 연도 추출 (ex. 2025)
//            String year = extractYearFromTitle(eventTitle);
//
//            for (WebElement bg : bgElements) {
//                // 날짜 문자열 예: "2025.06.27금 - 06.29일"
//                String dateText = bg.findElements(By.tagName("p")).get(1).getText().trim();
//                String[] parts = dateText.split("-");
//                if (parts.length < 2) continue; // 포맷이 예상과 다르면 스킵
//
//                String startPart = parts[0]; // "2025.06.27금"
//                String endPart = parts[1];   // "06.29일"
//
//                // 시작 날짜: 10글자 (yyyy.MM.dd), 나머지는 요일
//                String startDateStr = startPart.substring(0, 10);
//                String startDay = startPart.substring(10);
//
//                // 종료 날짜는 연도 붙여서 완성
//                String endDateStr = year + "." + endPart.substring(0, 5);
//                String endDay = endPart.substring(5);
//
//                try {
//                    Date startDate = dateFormat.parse(startDateStr);
//                    Date endDate = dateFormat.parse(endDateStr);
//
//                    startDates.add(startDate);
//                    endDates.add(endDate);
//                    startDays.add(startDay);
//                    endDays.add(endDay);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//                // 장소 정보 (도시 + 장소명)
//                String city = bg.findElement(By.cssSelector(".location")).getText().replaceAll("\\s", "");
//                String venue = bg.findElements(By.tagName("p")).get(2).getText().trim();
//                locations.add(city + " " + venue);
//            }
//
//            // 가장 빠른 시작 날짜와 가장 늦은 종료 날짜를 선택
//            Date minStartDate = startDates.stream().min(Date::compareTo).orElse(null);
//            Date maxEndDate = endDates.stream().max(Date::compareTo).orElse(null);
//
//            String minStartDay = (minStartDate != null) ? startDays.get(startDates.indexOf(minStartDate)) : "";
//            String maxEndDay = (maxEndDate != null) ? endDays.get(endDates.indexOf(maxEndDate)) : "";
//
//            String locationCombined = String.join(", ", locations);
//
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
//            String startDateFormatted = (minStartDate != null) ? sdf.format(minStartDate) + "(" + minStartDay + ")" : "";
//            String endDateFormatted = (maxEndDate != null) ? sdf.format(maxEndDate) + "(" + maxEndDay + ")" : "";
//
//            // 주요 필드를 기반으로 해시값 생성 (중복 판별용)
//            String hash = generateHash(eventTitle, startDateFormatted, endDateFormatted, locationCombined);
//
//            // DB에서 해시로 기존 이벤트 조회
//            Optional<PetEvent> existingEventOpt = petRepository.findByHash(hash);
//
//            PetEvent event;
//
//            if (existingEventOpt.isPresent()) {
//                // 기존 엔티티 존재하면 업데이트가 필요한지 확인
//                event = existingEventOpt.get();
//
//                // 변경 여부 체크
//                boolean isChanged = isEventChanged(event, eventTitle, startDateFormatted, endDateFormatted, locationCombined, capturedImagePath);
//
//                if (isChanged) {
//                    // 변경사항 반영해 새 객체 생성 (id 유지)
//                    PetEvent updatedEvent = PetEvent.builder()
//                            .id(event.getId())
//                            .source(event.getSource())
//                            .eventTitle(eventTitle)
//                            .eventUrl(event.getEventUrl())
//                            .location(locationCombined)
//                            .startDate(startDateFormatted)
//                            .endDate(endDateFormatted)
//                            .imagePath(capturedImagePath)
//                            .hash(event.getHash())
//                            .detailContent(event.getDetailContent())
//                            .build();
//                    petRepository.save(updatedEvent);
//                    event = updatedEvent; // 반환 리스트에 최신 객체 추가
//                }
//            } else {
//                // 신규 이벤트 저장
//                event = PetEvent.builder()
//                        .source("PET&MORD")
//                        .eventTitle(eventTitle)
//                        .eventUrl("https://ilovepets.co.kr/?main=0")
//                        .location(locationCombined)
//                        .startDate(startDateFormatted)
//                        .endDate(endDateFormatted)
//                        .imagePath(capturedImagePath)
//                        .hash(hash)
//                        .detailContent("")
//                        .build();
//                petRepository.save(event);
//            }
//
//            resultList.add(event);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            driver.quit();
//        }
//
//        return resultList;
//    }
//
//    /**
//     * 이벤트 제목에서 연도(4자리 숫자) 추출
//     */
//    private String extractYearFromTitle(String title) {
//        String year = "2025"; // 기본값
//        try {
//            year = title.replaceAll("[^0-9]", "").substring(0, 4);
//        } catch (Exception e) {
//            // 연도 추출 실패 시 기본값 유지
//        }
//        return year;
//    }
//
//    /**
//     * 주요 이벤트 필드로 MD5 해시 생성 (중복 판별용)
//     */
//    private String generateHash(String eventTitle, String startDate, String endDate, String location) {
//        try {
//            String source = eventTitle + startDate + endDate + location;
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] hashBytes = md.digest(source.getBytes());
//            StringBuilder sb = new StringBuilder();
//            for (byte b : hashBytes) {
//                sb.append(String.format("%02x", b));
//            }
//            return sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 기존 이벤트와 새 이벤트 정보를 비교해 변경 여부 판단
//     */
//    private boolean isEventChanged(PetEvent existing, String title, String startDate, String endDate, String location, String imagePath) {
//        return !existing.getEventTitle().equals(title)
//                || !existing.getStartDate().equals(startDate)
//                || !existing.getEndDate().equals(endDate)
//                || !existing.getLocation().equals(location)
//                || !existing.getImagePath().equals(imagePath);
//    }
//}
