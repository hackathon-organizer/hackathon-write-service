package com.hackathonorganizer.hackathonwriteservice.team.model.dto;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public record TagRequest(@NotEmpty @Size(max = 15) String name) {

}
