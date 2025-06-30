package com.playdata.petevent.util.service;

import com.playdata.petevent.util.entity.PetEvent;
import java.util.List;

public interface PetService {

    void saveEvents(List<PetEvent> events);

}
