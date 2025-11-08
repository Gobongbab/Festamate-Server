package com.gobongbob.festamate.event.handler;

import com.gobongbob.festamate.event.dto.SmsRequestDto;
import com.gobongbob.festamate.event.exception.PermanentFailureException;
import com.gobongbob.festamate.event.exception.RetryableException;
import com.gobongbob.festamate.event.util.JsonUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.exception.NurigoUnknownException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingMatchedSmsHandler implements EventHandler {

    private final JsonUtils jsonUtils;
    private final DefaultMessageService smsService;

    @Value("${coolsms.from.number}")
    private String fromNumber;

    @Override
    public String getEventType() {
        return "ROOM_MATCHED_SMS";
    }

    @Override
    public void handle(String payload) {
        SmsRequestDto requestDto = jsonUtils.fromJson(payload, SmsRequestDto.class);
        List<String> phoneNumbers = requestDto.phoneNumbers();

        phoneNumbers.forEach(phoneNumber -> {
            Message message = setMessage(phoneNumber, requestDto.title(), requestDto.openChatUrl());
            try {
                smsService.send(message);
            } catch (NurigoMessageNotReceivedException e) {
                throw new PermanentFailureException("SMS 수신 불가: " + phoneNumber, e);
            } catch (NurigoEmptyResponseException e) {
                throw new RetryableException("CoolSMS API 일시적 오류", e);
            } catch (NurigoUnknownException e) {
                throw new RetryableException("SMS 발송 중 예상치 못한 오류", e);
            }
        });
    }

    private Message setMessage(String phoneNumber, String title, String openChatUrl) {
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(phoneNumber);
        message.setText("[FestaMate!] 모임방 "
                + title
                + "에 매칭이 완료되었어요!  오픈채팅에 입장하여 시간과 장소를 정해보세요! \n "
                + "오픈채팅 링크: \n"
                + openChatUrl
        );

        return message;
    }
}