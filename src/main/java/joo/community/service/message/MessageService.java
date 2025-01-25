package joo.community.service.message;

import joo.community.dto.message.MessageCreateRequest;
import joo.community.dto.message.MessageDto;
import joo.community.entity.user.Message;
import joo.community.entity.user.User;
import joo.community.exception.MemberNotEqualsException;
import joo.community.exception.MemberNotFoundException;
import joo.community.exception.MessageNotFoundException;
import joo.community.repository.message.MessageRepository;
import joo.community.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto createMessage(MessageCreateRequest req) {

        User receiver = userRepository.findByNickname(req.getReceiverNickname()).orElseThrow(MemberNotFoundException::new);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User sender = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);
        Message message = new Message(req.getTitle(), req.getContent(), sender, receiver);
        messageRepository.save(message);

        return MessageDto.toDto(message);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> receiveMessages() { // 전체 메시지 수신

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        List<MessageDto> messageDtoList = new ArrayList<>();
        List<Message> messageList = messageRepository.findAllByReceiver(user);

        for (Message message : messageList) {
            if (!message.isDeletedByReceiver()) {
                messageDtoList.add(MessageDto.toDto(message));
            }
        }

        return messageDtoList;
    }

    @Transactional(readOnly = true)
    public MessageDto receiveMessage(Long id) { // 메시지 수신

        Message message = messageRepository.findById(id).orElseThrow(MessageNotFoundException::new);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        if (message.getReceiver() != user) {
            throw new MemberNotEqualsException();
        }

        if (message.isDeletedByReceiver()) {
            throw new MessageNotFoundException();
        }

        return MessageDto.toDto(message);
    }

    @Transactional
    public List<MessageDto> sendMessages() {

        // 검증 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 회원 검증 및 정보 가져오기
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        List<MessageDto> messageDtoList = new ArrayList<>();
        List<Message> messageList = messageRepository.findAllBySender(user);

        for (Message message : messageList) {
            if (!message.isDeletedBySender()) {
                messageDtoList.add(MessageDto.toDto(message));
            }
        }
        return messageDtoList;
    }

    @Transactional(readOnly = true)
    public MessageDto sendMessage(Long id) {

        Message message = messageRepository.findById(id).orElseThrow(MessageNotFoundException::new);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        if (message.getSender() != user) {
            throw new MemberNotEqualsException();
        }

        if (message.isDeletedByReceiver()) {
            throw new MessageNotFoundException();
        }

        return MessageDto.toDto(message);
    }

    @Transactional
    public void deleteMessageByReceiver(Long id) {

        Message message = messageRepository.findById(id).orElseThrow(MessageNotFoundException::new);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        if (message.getReceiver().equals(user)) {
            message.deleteByReceiver();
        } else {
            throw new MemberNotEqualsException();
        }

        if (message.isDeletedMessage()) {
            // 수신, 송신자 둘 다 삭제 할 경우
            messageRepository.delete(message);
        }
    }

    @Transactional
    public void deleteMessageBySender(Long id) {

        Message message = messageRepository.findById(id).orElseThrow(MemberNotFoundException::new);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        if (message.getSender().equals(user)) {
            message.deleteBySender();
        } else {
            throw new MemberNotEqualsException();
        }

        if (message.isDeletedMessage()) {
            messageRepository.delete(message);
        }
    }
}
