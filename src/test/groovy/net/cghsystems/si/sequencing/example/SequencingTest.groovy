package net.cghsystems.si.sequencing.example

import static org.junit.Assert.*

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.core.MessageHandler
import org.springframework.integration.core.SubscribableChannel
import org.springframework.integration.support.MessageBuilder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration("classpath:si-example-context.xml")
class SequencingTest {

    @Resource(name = "testInputChannel")
    private DirectChannel inputChannel

    @Resource(name = "testOutputChannel")
    private SubscribableChannel outputChannel

    @Test
    @DirtiesContext
    void shouldHandleNoSequenceOrder() {
        def inputs = []
        inputs << new InputModelObject(sequence: 3, name: "Test")
        inputs << new InputModelObject(sequence: 2, name: "Test")

        final countDownLatch = new CountDownLatch(inputs.size())

        //Should get messages in order (1-5)
        def x = 0
        final messageHandler = [handleMessage: {
                final sequence = it.getPayload().sequence
                assert sequence == ++x : "Messages are not in order Got sequence ${sequence} as position ${x}"
                countDownLatch.countDown()
            } ] as MessageHandler

        outputChannel.subscribe(messageHandler)

        //Send the messages in 5-1)
        inputs.each {
            inputChannel.send( MessageBuilder.withPayload(it).build() )
        }

        assert true == countDownLatch.await(3000, TimeUnit.MILLISECONDS) : "Message not recieved"
    }

    @Test
    @DirtiesContext
    void shouldGetSequencesFromDifferentCorrelationsInOrder() {
        def inputs = []
        inputs << new InputModelObject(sequence: 3, name: "Test")
        inputs << new InputModelObject(sequence: 2, name: "Test-1")
        inputs << new InputModelObject(sequence: 2, name: "Test")
        inputs << new InputModelObject(sequence: 1, name: "Test-1")
        inputs << new InputModelObject(sequence: 1, name: "Test")

        final test1CountDownLatch = new CountDownLatch(2)
        final testCountDownLatch = new CountDownLatch(3)

        //Should get messages in order (1-5)
        def (testCount, test1Count) = [0, 0]
        final messageHandler = [handleMessage: {
                final sequence = it.getPayload().sequence
                final correlationId = it.getHeaders().getCorrelationId()

                println correlationId

                if(correlationId == "Test-1") {
                    assert sequence == ++test1Count : "Messages are not in order. Got sequence ${sequence} as position ${test1Count}"
                    test1CountDownLatch.countDown()
                }else if(correlationId == "Test") {
                    assert sequence == ++testCount : "Messages are not in order. Got sequence ${sequence} as position ${testCount}"
                    testCountDownLatch.countDown()
                }
            } ] as MessageHandler

        outputChannel.subscribe(messageHandler)

        inputs.each {
            println it
            inputChannel.send( MessageBuilder.withPayload(it).build() )
        }

        assert true == test1CountDownLatch.await(3000, TimeUnit.MILLISECONDS) : "Message not recieved for correlationId: test"
        assert true == testCountDownLatch.await(3000, TimeUnit.MILLISECONDS) : "Message not recieved for correlationId: test-11"

    }

    @Test
    @DirtiesContext
    void shouldGetOutOrderSequenceMessagesInSequnceOrder() {
        def inputs = []
        inputs << new InputModelObject(sequence: 3, name: "Test")
        inputs << new InputModelObject(sequence: 1, name: "Test")
        inputs << new InputModelObject(sequence: 2, name: "Test")

        final countDownLatch = new CountDownLatch(inputs.size())

        //Should get messages in order (1-5)
        def x = 0
        final messageHandler = [handleMessage: {
                final sequence = it.getPayload().sequence
                assert sequence == ++x : "Messages are not in order Got sequence ${sequence} as position ${x}"
                countDownLatch.countDown()
            } ] as MessageHandler

        outputChannel.subscribe(messageHandler)

        //Send the messages in 5-1)
        inputs.each {
            inputChannel.send( MessageBuilder.withPayload(it).build() )
        }

        assert true == countDownLatch.await(3000, TimeUnit.MILLISECONDS) : "Message not recieved"
    }

    @Test
    @DirtiesContext
    void shouldGetReverseSequnceMessagesInSequenceOrder() {

        def inputs = []
        //Create the messages in reverse order (5-1)
        5.downto 1, {
            inputs << new InputModelObject(sequence: it, name: "Test")
        }

        final countDownLatch = new CountDownLatch(inputs.size())

        def order = [1, 2, 3, 4, 5]

        //Should get messages in order (1-5)
        def x = 0
        final messageHandler = [handleMessage: {
                final sequence = it.getPayload().sequence
                assert sequence == ++x : "Messages are not in order Got sequence ${sequence} as position ${x}"
                countDownLatch.countDown()
            } ] as MessageHandler

        outputChannel.subscribe(messageHandler)

        //Send the messages in 5-1)
        inputs.each {
            inputChannel.send( MessageBuilder.withPayload(it).build() )
        }

        assert true == countDownLatch.await(3000, TimeUnit.MILLISECONDS) : "Message not recieved"
    }
}
