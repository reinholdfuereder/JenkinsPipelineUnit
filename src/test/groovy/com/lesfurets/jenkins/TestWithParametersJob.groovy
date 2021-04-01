package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.Before
import org.junit.Test

class TestWithParametersJob extends BaseRegressionTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }

    @Test
    void should_run_script_with_parameters() {
        // TODO: Must override default from com/lesfurets/jenkins/unit/BasePipelineTest.groovy:214
        //   because com.lesfurets.jenkins.unit.BasePipelineTest.stringInterceptor seems to return null!?
        helper.registerAllowedMethod("string", [Map], { Map map ->
            return map
        })

        helper.registerAllowedMethod("parameters", [List], { List params ->
            params.forEach { param ->
                if (param instanceof Map) {
                    addParam(param.name as String, param.defaultValue, false)
                } else {
                    throw new IllegalStateException("Unexpected parameters param: ${param}")
                }
            }
        })

        // when:
        runScript("job/withParameters.jenkins")

        // then:
        assertJobStatusSuccess()
        testNonRegression("withParameters")
    }
}
