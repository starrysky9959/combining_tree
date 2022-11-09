<!--
 * @Author: starrysky9959 starrysky9651@outlook.com
 * @Date: 2022-11-09 00:12:39
 * @LastEditors: starrysky9959 starrysky9651@outlook.com
 * @LastEditTime: 2022-11-09 23:54:14
 * @Description:  
-->
# Combining Tree
reference: [https://www.elsevier.com/books-and-journals/book-companion/9780124159501/example-programs](https://www.elsevier.com/books-and-journals/book-companion/9780124159501/example-programs) Chapter 12

通过Unit Test, 没有重复或遗漏计数.

---

任务: 计数器从0累加到1000000.
- Combining Tree 使用10个线程, 每个执行100000次. 5次平均耗时1033.6ms
- 普通的顺序执行算法平均耗时11ms
- 之前的Spin Lock完成相同任务的耗时记录如下表所示

||10 thread|
|---|---|
|ALock|224.8ms|
|BackoffLock|437.6ms|
|CLHLock|287.2ms|
|MCSLock|274.6ms|
|TASLock|334.2ms|
|TTASLock|231.8ms|





