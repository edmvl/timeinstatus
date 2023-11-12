package ru.itq.timeinstatus.controller;


import ru.itq.timeinstatus.service.ExcelGeneratorService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("/excel")
public class RestController {

    private final ExcelGeneratorService excelGeneratorService;

    public RestController(ExcelGeneratorService excelGeneratorService) {
        this.excelGeneratorService = excelGeneratorService;
    }

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Object getBranches(@QueryParam(value = "issueIds") String issueIds) {
        List<Long> issueIdList = Arrays.stream(issueIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        ByteArrayOutputStream generate = excelGeneratorService.generate(issueIdList);
        return Response
                .ok()
                .entity(generate.toByteArray())
                .header("Content-Disposition", "attachment; filename=\"report" + LocalDateTime.now() + ".xlsx\"")
                .build();
    }

}
