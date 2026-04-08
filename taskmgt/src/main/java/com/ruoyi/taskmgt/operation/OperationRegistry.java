//package com.ruoyi.taskmgt.operation;
//
//import com.ruoyi.common.enums.ReturnNo;
//import com.ruoyi.common.exception.task.TaskmgtException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.i18n.LocaleContextHolder;
//import org.springframework.context.support.MessageSourceAccessor;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class OperationRegistry {
//
//    private final MessageSourceAccessor messageSourceAccessor;
//    private final Map<Long, OperationHandler> handlerMap = new ConcurrentHashMap<>();
//    private static final Logger log = LoggerFactory.getLogger(OperationRegistry.class);
//    public OperationRegistry(MessageSourceAccessor messageSourceAccessor) {
//        this.messageSourceAccessor = messageSourceAccessor;
//    }
//
//    @Autowired
//    public void registerHandlers(List<OperationHandler> handlers) {
//        for (OperationHandler handler : handlers) {
//            handlerMap.put(handler.getOperationId(), handler);
//            log.info("注册操作处理器: {} - {}", handler.getOperationId(), handler.getOperationName());
//        }
//    }
//
//    public OperationHandler getHandler(Long operationId) {
//        OperationHandler handler = handlerMap.get(operationId);
//        if (handler == null) {
//            String[] args = {messageSourceAccessor.getMessage("Operation.name", LocaleContextHolder.getLocale()), operationId.toString()};
//            throw new TaskmgtException(ReturnNo.RESOURCE_ID_NOTEXIST, args,
//                    messageSourceAccessor.getMessage(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
//        }
//        return handler;
//    }
//
//    public boolean supports(Long operationId) {
//        return handlerMap.containsKey(operationId);
//    }
//}