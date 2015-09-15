package org.kurento.tutorial.one2onecall.exception;

import java.io.IOException;
import org.kurento.tutorial.one2onecall.data.AppError;
import org.kurento.tutorial.one2onecall.data.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WSUtils {

    private static final Logger log = LoggerFactory.getLogger(WSUtils.class);

    public static void manageIfException(WebSocketSession session,
            String methodName,
            ExceptionMethodWrapper method
    ) {
        try {
            method.exec();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            InternalErrorCodes iec;

            if (t instanceof AppException) {
                AppException ae = (AppException) t;
                iec = ae.getErrorCode();
            } else {
                iec = InternalErrorCodes.UNHANDLED_ERROR;
            }

            Response resp = new Response.ResponseBuilder<>(methodName).status(iec.getStatus().intValue())
                    .error(new AppError(iec.getMessage(), t.getMessage(), iec.getCode()))
                    .build();

            try {
                session.sendMessage(new TextMessage(resp.toJsonStr()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }
    
    public static void sendMessage(WebSocketSession session,String message) throws IOException{
        session.sendMessage(new TextMessage(message));
    }
}
