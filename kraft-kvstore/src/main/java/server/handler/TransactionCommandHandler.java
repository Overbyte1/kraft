package server.handler;

import common.message.response.Response;
import server.store.KVStore;

public interface TransactionCommandHandler extends CommandHandler {
    //与存储引擎交互完成处理命令，用以支持事务。如果发生异常无法合理处理，应该将异常抛出以完成事务的回滚
    Response doHandle(Object command, KVStore kvStore);
}
