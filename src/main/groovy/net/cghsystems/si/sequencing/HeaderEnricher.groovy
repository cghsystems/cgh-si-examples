package net.cghsystems.si.sequencing

import org.springframework.integration.Message
import org.springframework.integration.MessageHeaders
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.transformer.Transformer


class HeaderEnricher implements Transformer {

    @Override
    public Message<?> transform(Message<?> message) {

        def payload = message.getPayload()

        def headers = new HashMap(message.headers)
        headers.put(MessageHeaders.SEQUENCE_NUMBER,  payload.sequence)
        headers.put(MessageHeaders.CORRELATION_ID, payload.name)

        MessageBuilder.withPayload(payload).copyHeaders(headers).build()
    }
}
