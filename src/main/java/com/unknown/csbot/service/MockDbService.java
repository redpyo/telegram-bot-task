package com.unknown.csbot.service;

import com.unknown.csbot.model.IdChangeData;
import com.unknown.csbot.model.InquiryData;
import com.unknown.csbot.model.RegistrationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockDbService implements DbService {

    @Override
    public boolean saveRegistration(RegistrationData data) {
        log.info("[Mock DB] 회원가입 저장: {}", data);
        return true;
    }

    @Override
    public boolean saveIdChange(IdChangeData data) {
        log.info("[Mock DB] 아이디 변경 저장: {}", data);
        return true;
    }

    @Override
    public boolean saveInquiry(InquiryData data) {
        log.info("[Mock DB] 문의 저장: {}", data);
        return true;
    }
}
