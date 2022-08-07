package com.hackathonorganizer.hackathonwriteservice.hackathon.utils;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HackathonMapper {

    public static HackathonResponse mapToDto(Hackathon hackathon) {

        return new HackathonResponse(hackathon.getId(), hackathon.getName(),
                hackathon.getDescription());
    }
}
