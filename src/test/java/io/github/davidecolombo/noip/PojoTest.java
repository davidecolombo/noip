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

/**
 * Tests for POJO (Plain Old Java Object) structure and behavior.
 * 
 * Uses OpenPOJO library to validate that model classes follow proper
 * Java bean conventions:
 * - All fields have getters
 * - All fields have setters (except static final)
 * - No public fields (except static final constants)
 * 
 * Classes tested: IpifyResponse, NoIpResponse, NoIpSettings
 */
class PojoTest {

    /**
     * Validates that all model classes have proper POJO structure.
     * 
     * For each class (IpifyResponse, NoIpResponse, NoIpSettings),
     * verifies:
     * - Getter methods exist for all fields
     * - Setter methods exist for all fields
     * - No public non-static final fields
     */
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