package joo.community.repository.message;

import joo.community.entity.user.Message;
import joo.community.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByReceiver(User user);

    List<Message> findAllBySender(User user);
}
