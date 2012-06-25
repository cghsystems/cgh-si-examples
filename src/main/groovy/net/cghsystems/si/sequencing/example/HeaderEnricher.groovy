package net.cghsystems.si.sequencing.example

import org.springframework.integration.Message
import org.springframework.integration.MessageHeaders
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.transformer.Transformer


class HeaderEnricher implements Transformer {

    @Override
    public Message<?> transform(Message<?> message) {

        def payload = message.getPayload()

        def (sequence, correlationId) = [
            payload.sequence,
            payload.name
        ]

        def headers = new HashMap(message.headers)
        headers.put(MessageHeaders.SEQUENCE_NUMBER, sequence)
        headers.put(MessageHeaders.CORRELATION_ID, correlationId)

        MessageBuilder.withPayload(payload).copyHeaders(headers).build()
    }
}
