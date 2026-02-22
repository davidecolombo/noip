package io.github.davidecolombo.noip;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsExceptStaticFinalRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.jupiter.api.Test;
import io.github.davidecolombo.noip.ipify.IpifyResponse;
import io.github.davidecolombo.noip.NoIpResponse;
import io.github.davidecolombo.noip.NoIpSettings;

class PojoTest {

    @Test
    void testPojoStructureAndBehavior() {
        for (Class<?> clazz : new Class[]{
                IpifyResponse.class,
                NoIpResponse.class,
                NoIpSettings.class
        }) {
            ValidatorBuilder.create()
                    .with(new GetterMustExistRule())
                    .with(new SetterMustExistRule())
                    .with(new GetterTester())
                    .with(new SetterTester())
                    .with(new NoPublicFieldsExceptStaticFinalRule())
                    .build()
                    .validate(PojoClassFactory.getPojoClass(clazz));
        }
    }
}