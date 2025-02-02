package org.gooinpro.gooinproadminapi.chatmessage.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gooinpro.gooinproadminapi.chatmessage.domain.ChatEntity;
import org.gooinpro.gooinproadminapi.chatmessage.dto.ChatMessageDTO;
import org.gooinpro.gooinproadminapi.chatmessage.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/chatmessage")
@RequiredArgsConstructor
@Log4j2
public class ChatController {

    private final SimpMessageSendingOperations template;

    private final ChatService chatService;

    // 채팅 리스트 반환
    @GetMapping("/chat")
    public ResponseEntity<List<ChatEntity>> getChatMessages(@RequestParam String sender, @RequestParam String receiver) {
        log.info("getChatMessages called for sender: {} and receiver: {}", sender, receiver);
        List<ChatEntity> chatEntities = chatService.getMessage(sender, receiver);
        return ResponseEntity.ok().body(chatEntities);
    }

    //메시지 송신 및 수신, /pub가 생략된 모습. 클라이언트 단에선 /pub/message로 요청
    @MessageMapping("/message")
    public ResponseEntity<Void> receiveMessage(@RequestBody ChatMessageDTO chat) {
        // 메시지를 해당 채팅방 구독자들에게 전송
        Long roomId = Long.parseLong(chat.getRoomId());
        template.convertAndSend("/sub/chatroom/" + roomId, chat);
        log.info("Sent message: {}", chat);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<ChatEntity> sendMessage(@RequestBody ChatMessageDTO chatMessageDTO) {

        log.info("sendMessage called");
        log.info("----------------------------------------");
        // 메시지를 서비스로 전달하여 저장
        ChatEntity savedMessage = chatService.saveMessage(chatMessageDTO);
        return ResponseEntity.ok(savedMessage);
    }
}
