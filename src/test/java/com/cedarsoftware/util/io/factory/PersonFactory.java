package com.cedarsoftware.util.io.factory;

import com.cedarsoftware.util.io.JsonReader.ClassFactory;
import com.cedarsoftware.util.io.TestCustomWriter;

import java.util.Map;

public class PersonFactory implements ClassFactory {
    @Override
    public Object newInstance(Class c, Object o, Map args) {
        return new TestCustomWriter.Person();
    }
}
