package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.Tag;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TagRequest;
import com.hackathonorganizer.hackathonwriteservice.team.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
@Slf4j
public class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tag create(@Valid @RequestBody TagRequest tagRequest) {
        log.info("Processing new tag create request {}", tagRequest);
        return tagService.create(tagRequest);
    }

    @PutMapping("/{id}")
    public Tag edit(@PathVariable Long id,
            @RequestBody @Valid TagRequest tagRequest) {
        log.info("Processing new tag edit id: {} request {}", id, tagRequest);
        return tagService.editById(id, tagRequest);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tagService.deleteById(id);
    }
}
