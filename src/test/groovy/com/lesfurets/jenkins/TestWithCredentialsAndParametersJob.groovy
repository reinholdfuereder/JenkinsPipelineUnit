package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.Before
import org.junit.Test

class TestWithCredentialsAndParametersJob extends BaseRegressionTest {

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

        // TODO: Because of overriding stringInterceptor,
        helper.registerAllowedMethod("withCredentials", [List, Closure], withCredentialsInterceptorWhenStringInterceptorIsOverridden)

        // when:
        runScript("job/withCredentialsAndParameters.jenkins")

        // then:
        assertJobStatusSuccess()
        testNonRegression("withCredentialsAndParameters")
    }


    /**
     * TODO: Adapted copy of com.lesfurets.jenkins.unit.BasePipelineTest#withCredentialsInterceptor to support "string"
     *   credentials (and parameters)
     */
    def withCredentialsInterceptorWhenStringInterceptorIsOverridden = { list, closure ->
        def previousValues = [:]
        list.forEach { creds ->
            // stringInterceptor returns a String value where the
            // usernamePasswordInterceptor returns a list of strings
            if (creds instanceof String) {
                try {
                    previousValues[creds] = binding.getVariable(creds)
                } catch (MissingPropertyException e) {
                    previousValues[creds] = null
                }
                binding.setVariable(creds, creds)
            } else if (creds instanceof Map && creds.get("\$class") == "UsernamePasswordMultiBinding") {
                def username = creds.get("usernameVariable")
                def password = creds.get("passwordVariable")
                try {
                    previousValues[username] = binding.getVariable(username)
                } catch (MissingPropertyException e) {
                    previousValues[username] = null
                }
                binding.setVariable(username, username)
                try {
                    previousValues[password] = binding.getVariable(password)
                } catch (MissingPropertyException e) {
                    previousValues[password] = null
                }
                binding.setVariable(password, password)
            } else {
                creds.each { var ->
                    // TODO: New block is needed, because stringInterceptor is overridden:
                    if (var instanceof Map.Entry) {
                        var = var.value
                    }

                    try {
                        previousValues[var] = binding.getVariable(var)
                    } catch (MissingPropertyException e) {
                        previousValues[var] = null
                    }
                    binding.setVariable(var, var)
                }
            }
        }

        closure.delegate = delegate
        def res = helper.callClosure(closure)

        // If previous value was not set it will unset by using null
        // otherwise it will restore previous value.
        previousValues.each { key, value ->
            binding.setVariable(key, value)
        }
        return res
    }

}
