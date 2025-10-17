package com.itheima.config;
import java.util.Set;
import io.grpc.*;

public class HelloClientInterceptor implements ClientInterceptor {

@Override
public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
        CallOptions callOptions, Channel channel) {
    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            channel.newCall(methodDescriptor, callOptions)) {

        @Override
        public void sendMessage(ReqT message) {
            System.out.printf("Sending method '%s' message '%s'%n", methodDescriptor.getFullMethodName(),
                    message.toString());
            super.sendMessage(message);
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            System.out.println(HelloClientInterceptor.class.getSimpleName());

            ClientCall.Listener<RespT> listener = new ForwardingClientCallListener<RespT>() {
                @Override
                protected Listener<RespT> delegate() {
                    return responseListener;
                }

                @Override
                public void onMessage(RespT message) {
                    System.out.printf("Received message '%s'%n", message.toString());
                    super.onMessage(message);
                }
            };

            super.start(listener, headers);
        }
    };
}

}