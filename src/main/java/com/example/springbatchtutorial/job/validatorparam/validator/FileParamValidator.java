package com.example.springbatchtutorial.job.validatorparam.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

@Slf4j
public class FileParamValidator implements JobParametersValidator {

    @Override
    public void validate(final JobParameters parameters) throws JobParametersInvalidException {
        String fileName = parameters.getString("fileName");
        log.info("filename : {}", fileName);

        if (!StringUtils.endsWithIgnoreCase(fileName, "csv")) {
            throw new JobParametersInvalidException("This is not csv file");
        }
    }
}
