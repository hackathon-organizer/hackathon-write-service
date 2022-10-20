package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.CriteriaAnswer;

public record CriteriaAnswerRequest (
        Long id,
        String name,
        CriteriaAnswer criteriaAnswer
) {}