package com.example.myShop.service;

import com.example.myShop.dto.PayApproveRequestDto;
import com.example.myShop.dto.PayApproveResponseDto;
import com.example.myShop.dto.PayRequestDto;
import com.example.myShop.entity.Item;
import com.example.myShop.entity.PaymentTemp;
import com.example.myShop.repository.ItemRepository;
import com.example.myShop.repository.PaymentTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor

public class KakaoPayService {

    private static final String HOST = "https://kapi.kakao.com";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ItemRepository itemRepository;
    private final PaymentTempRepository paymentTempRepository;

    //주문하기 눌렀을 때, QR 요청하는 메서드입니다.
    public String kakaoPayReady(PayRequestDto dto) {
        try {
            // 1. 상품 정보 조회
            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            int quantity = dto.getCount();
            int totalAmount = item.getPrice() * quantity;

            // 2. 요청 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "KakaoAK YOUR ADMIN KEY"); // 카카오페이 Admin Key 발급 받으면 됩니다.
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // 3. 요청 파라미터
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("cid", "TC0ONETIME"); // 테스트용 cid
            String partnerOrderId = dto.getPartnerOrderId();
            params.add("partner_order_id", partnerOrderId);
            params.add("partner_user_id", dto.getEmail());
            params.add("item_name", item.getItemName());
            params.add("quantity", String.valueOf(quantity));
            params.add("total_amount", String.valueOf(totalAmount));
            params.add("vat_amount", "0");
            params.add("tax_free_amount", "0");
            params.add("approval_url", "http://localhost/kakaoPaySuccess");//paycontroller에 매핑됨
            params.add("cancel_url", "http://localhost/kakaoPayCancel");
            params.add("fail_url", "http://localhost/kakaoPayFail");

            //위 헤더랑 파라미터
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 4. 카카오페이 서버에 결제 준비 요청
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    new URI(HOST + "/v1/payment/ready"),//QR 요청 받는 카카오 url임.
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>(){}
            );

            //요청 성공시
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                assert body != null;

                //요청 성공 했으니 결제 고유번호 반환함.
                String tid = body.get("tid").toString();

                //결제정보 임시저장
                PaymentTemp payment = new PaymentTemp();
                payment.setTid(tid);
                payment.setItemId(dto.getItemId());
                payment.setCount(quantity);
                payment.setEmail(dto.getEmail());
                payment.setPartnerOrderId(partnerOrderId);
                paymentTempRepository.save(payment);

                log.info("카카오페이 결제 준비 성공: {}", body);
                return body.get("next_redirect_pc_url").toString();
            } else {
                log.error("카카오페이 결제 준비 실패: {}", response);
                return "/pay/error";
            }

        } catch (Exception e) {
            log.error("카카오페이 요청 중 예외 발생", e);
            return "/pay/error";
        }
    }

    //우리가 QR 찍은 후의 결제승인요청
    public PayApproveResponseDto kakaoPayApprove(PayApproveRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK YOUR ADMIN KEY");
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", dto.getTid());
        params.add("partner_order_id", dto.getPartnerOrderId());
        params.add("partner_user_id", dto.getPartnerUserId());
        params.add("pg_token", dto.getPgToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    new URI(HOST + "/v1/payment/approve"),
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> resBody = response.getBody();

            if (resBody == null) {
                throw new RuntimeException("카카오 응답이 비어있음");
            }

            PayApproveResponseDto res = new PayApproveResponseDto();
            res.setTid(resBody.get("tid").toString());
            res.setPartnerOrderId(resBody.get("partner_order_id").toString());
            res.setPartnerUserId(resBody.get("partner_user_id").toString());
            res.setItemName(resBody.get("item_name").toString());
            res.setApprovedTime(resBody.get("approved_at").toString());
            res.setTotalAmount((Integer) ((Map<String, Object>) resBody.get("amount")).get("total"));
            return res;

        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            log.error("🔥 카카오페이 승인 실패 응답: {}", body);
            throw new RuntimeException("카카오 결제 승인 실패: " + body);
        } catch (Exception e) {
            log.error("💥 승인 요청 중 일반 예외", e);
            throw new RuntimeException("결제 승인 실패: " + e.getMessage());
        }
    }




}
