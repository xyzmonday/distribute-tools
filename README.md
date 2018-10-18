# 分布式基础组件研究
## 分布式锁
- 基于mysql实现：阻塞，重入，性能低
- 基于redis实现：阻塞，性能高
- 基于zk实现：阻塞，性能高 
以上三种分布式锁都在秒杀场景下测试通过，能够完全保证库存的正确。具体三种分布式锁的实现过程和讨论 可以参考distribute-lock项目doc文档部分。
