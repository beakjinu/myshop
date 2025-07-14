package com.example.myShop.controller;

import com.example.myShop.dto.OrderDto;
import com.example.myShop.dto.PayApproveRequestDto;
import com.example.myShop.dto.PayApproveResponseDto;
import com.example.myShop.dto.PayRequestDto;
import com.example.myShop.entity.Order;
import com.example.myShop.entity.PaymentTemp;
import com.example.myShop.repository.ItemRepository;
import com.example.myShop.repository.PaymentTempRepository;
import com.example.myShop.service.KakaoPayService;
import com.example.myShop.service.OrderService;
import com.example.myShop.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PayController {

    private final KakaoPayService kakaoPayService;
    private final OrderService orderService;
    private final SlackService slackService;
    private final ItemRepository itemRepostitory;
    private final PaymentTempRepository paymentTempRepository;

    @PostMapping(value = "/pay")//itemdetail.html의 order에서 url을 /pay로 바꾼거에요
    @ResponseBody
    public ResponseEntity<Map<String,String>> kakaoPay(@RequestBody PayRequestDto payRequestDto, Principal principal) {
        // KakaoPayService redirect URL 받아서 응답
        String email = principal.getName();
        payRequestDto.setEmail(email);

        String partnerOrderId = "order_" + UUID.randomUUID();
        payRequestDto.setPartnerOrderId(partnerOrderId);

        String url = kakaoPayService.kakaoPayReady(payRequestDto);

        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", url);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/kakaoPaySuccess")//위 QR코드에서 결제 성공시 호출
    @ResponseBody
    public String kakaoPaySuccess(@RequestParam String pg_token,Principal principal, Model model) {
        try {
            String email = principal.getName();

            PaymentTemp payment = paymentTempRepository.findTopByEmailOrderByCreatedAtDesc(email)
                    .orElseThrow(() -> new IllegalStateException("결제 정보가 존재하지 않습니다."));


            String tid = payment.getTid();
            String partnerOrderId = payment.getPartnerOrderId();


            int count = payment.getCount();
            Long itemId = payment.getItemId();

            //세션이 유효한가?
            if (itemId == null || count == 0 || email == null) {
                model.addAttribute("msg", "세션 정보가 유효하지 않습니다.");
                slackService.sendMessage("결제 실패 : 세션 정보 없음");
                return "redirect:/?fail";
            }

            //결제 승인 DTO
            PayApproveRequestDto approveDto = new PayApproveRequestDto();
            approveDto.setTid(tid);
            approveDto.setPgToken(pg_token);
            approveDto.setPartnerOrderId(partnerOrderId);
            approveDto.setPartnerUserId(payment.getEmail());

            PayApproveResponseDto kakaoRes = kakaoPayService.kakaoPayApprove(approveDto);

            //결제 승인 요청
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(itemId);
            orderDto.setCount(count);
            orderDto.setTid(kakaoRes.getTid());//response의 Tid 담음

            String itemName = itemRepostitory.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."))
                    .getItemName();

            Long orderId = orderService.order(orderDto, email);

            //슬랙에 메세지 보냄
            slackService.sendMessage("[Team4]에서 알려드립니다.\n"  + email + "님의 주문이 완료되었습니다.\n 주문번호 : " + orderId + "\n 주문 상품 : " + itemName);

            paymentTempRepository.deleteById(tid);

            //확인창 나오고 메인으로 이동
            return "<script>alert('결제가 완료되었습니다! 주문번호: " + orderId + "'); location.href='/'</script>";
        } catch (Exception e){
            model.addAttribute("msg", "결제 중 오류 발생" + e.getMessage());
            String email = principal.getName();
            slackService.sendMessage("[Team4}에서 알려드립니다. 결제 중 오류가 발생했습니다.\n 주문자 : " + email);
            return "<script>alert('결제 중 오류 발생: " + e.getMessage() + "'); location.href='/'</script>";
        }
    }

    //아직
    @GetMapping(value = "/kakaoPayCancel")
    public String kakaoPayCancel(){
        return "redirect:/order/cancel";
    }

    @GetMapping(value ="/kakaoPayFail")
    public String kakaoPayFail(){
        return "redirect:/order/fail";
    }
}
