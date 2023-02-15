package com.hackathonorganizer.hackathonwriteservice.utils;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.CriteriaAnswer;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaAnswerDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HackathonMapper {

    public static HackathonResponse mapToDto(Hackathon hackathon) {

        return new HackathonResponse(hackathon.getId(), hackathon.getName(),
                hackathon.getDescription());
    }

    public static CriteriaDto mapToCriteriaDto(Criteria criteria) {

        return new CriteriaDto(
                criteria.getId(),
                criteria.getName(),
                criteria.getHackathon().getId()
        );
    }

    public static CriteriaAnswerDto mapToCriteriaAnswerDto(CriteriaAnswer criteriaAnswer) {
        return new CriteriaAnswerDto(
                criteriaAnswer.getId(),
                criteriaAnswer.getCriteria().getId(),
                criteriaAnswer.getValue(),
                criteriaAnswer.getTeamId(),
                criteriaAnswer.getUserId()
        );
    }
}
