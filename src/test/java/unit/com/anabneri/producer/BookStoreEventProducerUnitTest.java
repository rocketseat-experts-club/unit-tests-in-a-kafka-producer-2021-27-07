package com.anabneri.producer;

import com.anabneri.domain.Book;
import com.anabneri.domain.BookStoreEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import scala.Int;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookStoreEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    // o object mapper serializa o objeto
    @Spy
    ObjectMapper objectMapper;

    @InjectMocks
    BookStoreEventProducer bookStoreEventProducer;

    @Test
    void should_validate_book_store_event_with_approach2_on_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        Book book = Book.builder()
                .bookId(null)
                .bookName("A culpa e das estrelas")
                .bookAuthor(null)
                .build();


        BookStoreEvent bookStoreEvent = BookStoreEvent.builder()
                .bookStoreEventId(null)
                .book(book)
                .build();

        SettableListenableFuture future = new SettableListenableFuture();


        future.setException(new RuntimeException("Calling a Kafka Exception here"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        //when

        assertThrows(Exception.class, ()-> bookStoreEventProducer.senBookStoreEvent_Approach2(bookStoreEvent).get());

    }

    @Test
    void should_validate_book_store_event_with_approach2_on_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        Book book = Book.builder()
                .bookId(null)
                .bookName("A culpa e das estrelas")
                .bookAuthor(null)
                .build();


        BookStoreEvent bookStoreEvent = BookStoreEvent.builder()
                .bookStoreEventId(null)
                .book(book)
                .build();

        String record = objectMapper.writeValueAsString(bookStoreEvent);
        SettableListenableFuture future = new SettableListenableFuture();

        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("bookstore-events", bookStoreEvent.getBookStoreEventId(), record);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("bookstore-events", 1),1,1,342, System.currentTimeMillis(), 1,2);
        SendResult<Integer, String> sendResult = new SendResult<Integer, String>(producerRecord, recordMetadata);

        future.set(sendResult);
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        //when
        ListenableFuture<SendResult<Integer, String>> listenableFuture = bookStoreEventProducer.senBookStoreEvent_Approach2(bookStoreEvent);

        //then
       SendResult<Integer, String> sendResult1  = listenableFuture.get();
       assert sendResult1.getRecordMetadata().partition() == 1;

    }
}
