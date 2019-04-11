# Apache-Commons-Collections
## 1.反序列化的原因

如果Java应用对用户输入的内容做了序列化，但是用户输入的内容是不可信的数据，进行了恶意构造，那么在对恶意构造的数据做反序列化处理，就会产生的非预期对象，在这个过程中就可能带来任意代码执行。

## 2.漏洞原理

在进行Map转换时，修改Map中的key和value会调用接口，触发内置的transform来获取类名、方法对象进行反射，其中传入的方法名，参数类型等都是可控的，那么我们就可以构造ChainedTransformer来进行反射的方式调用函数

## 3.影响版本

commons-collections<=3.2.1存在漏洞

## 4.下载地址

本文对commons-collections-3.2.1进行漏洞分析，下载地址：[https://github.com/SQYY/Apache-Commons-Collections/raw/master/commons-collections-3.2.1-1.0.0.jar](https://github.com/SQYY/Apache-Commons-Collections/raw/master/commons-collections-3.2.1-1.0.0.jar)

## 5.漏洞分析

在`org/apache/commons/collections/map/TransformedMap.class`中可以看到**decorate()**方法，它是将一个Map对象，转换成TransformedMap对象。

![](https://ws1.sinaimg.cn/large/006tKfTcgy1g0u8z23gvej31n80madk9.jpg)

可以看到它会把它的三个参数带入到**TransformedMap**方法中，并返回实例化的结果。而在这个方法中，如上图所示，就是将**Map**中的**key**和**value**转换成**TransformedMap**中的**key**和**value**

而`transform`是一个接口，即可实现数据变换的操作。当Map中的任意项的Key或者value被修改，这个接口就会被调用。

![](https://ws2.sinaimg.cn/large/006tKfTcgy1g0uervni1rj313805udgn.jpg)

在`org/apache/commons/collections/functors/InvokerTransformer.class`中，也是我们核心的关键点，可以看到它实现了**transform**和**Serializable**这两个接口

![](https://ws2.sinaimg.cn/large/006tKfTcgy1g0uay7bi1fj31aw0f6ad0.jpg)

可以看到**transform**中，首先会**getClass**获取传入参数的类名，再通过**getMethod**获取方法对象，最后调用**invoke**进行反射

![](https://ws3.sinaimg.cn/large/006tKfTcgy1g0ubgo5abkj31kj0u0wlq.jpg)

就是说只需要传入方法名、参数类型和参数，我们就可以通过Java的反射机制可以调用任意函数。

## 6.构造POC

那么我们重要的是多个Transformer能串联起来，形成ChainedTransformer，让它依次执行便可序列化。

我们要构造一个实例，修改其中的数据，让它依次调用**Runtime** -->**getRuntime()**-->**exec()**，执行`Runtime.getRuntime().exec()`，便可序列化

首先需要**ConstantTransformer()**获取了**Runtime**类，接着调用**getMethod**方法来定位需要查找的**getRuntime**，再反射调用**getRuntime**中的的**exec()**，最终执行命令

![](https://ws3.sinaimg.cn/large/006tKfTcgy1g0uc3wuqosj31ju0d8n0f.jpg)

而**decorate()**就是将**Map**转换成**TransformedMap**，这里我们再看一遍这个方法，它的第三个参数就是Transformer类型

![](https://ws1.sinaimg.cn/large/006tKfTcgy1g0u8z23gvej31n80madk9.jpg)

那么我们可以将构造的内容当做第三个参数传入进去

![](https://ws3.sinaimg.cn/large/006tKfTcgy1g0ucamht5uj318i070dha.jpg)

完整POC地址：[https://github.com/SQYY/Apache-Commons-Collections/blob/master/POC.java](<https://github.com/SQYY/Apache-Commons-Collections/blob/master/POC.java>)


## 6.参考链接

* [https://paper.seebug.org/312/#6-java-apache-commonscollections-rce](https://paper.seebug.org/312/#6-java-apache-commonscollections-rce)

* [https://xz.aliyun.com/t/136](https://xz.aliyun.com/t/136)

* [http://pirogue.org/2017/12/22/javaSerialKiller/](http://pirogue.org/2017/12/22/javaSerialKiller/)





## 6.参考链接

* [https://paper.seebug.org/312/#6-java-apache-commonscollections-rce](https://paper.seebug.org/312/#6-java-apache-commonscollections-rce)

* [https://xz.aliyun.com/t/136](https://xz.aliyun.com/t/136)

* [http://pirogue.org/2017/12/22/javaSerialKiller/](http://pirogue.org/2017/12/22/javaSerialKiller/)



