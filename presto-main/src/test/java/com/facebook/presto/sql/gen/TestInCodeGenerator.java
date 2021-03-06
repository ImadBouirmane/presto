/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.gen;

import com.facebook.presto.metadata.Signature;
import com.facebook.presto.sql.gen.InCodeGenerator.SwitchGenerationCase;
import com.facebook.presto.sql.relational.CallExpression;
import com.facebook.presto.sql.relational.ConstantExpression;
import com.facebook.presto.sql.relational.RowExpression;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.facebook.presto.metadata.FunctionType.SCALAR;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.DateType.DATE;
import static com.facebook.presto.spi.type.DoubleType.DOUBLE;
import static com.facebook.presto.sql.gen.InCodeGenerator.SwitchGenerationCase.DIRECT_SWITCH;
import static com.facebook.presto.sql.gen.InCodeGenerator.SwitchGenerationCase.HASH_SWITCH;
import static com.facebook.presto.sql.gen.InCodeGenerator.SwitchGenerationCase.SET_CONTAINS;
import static com.facebook.presto.sql.gen.InCodeGenerator.checkSwitchGenerationCase;
import static com.facebook.presto.sql.relational.Signatures.CAST;

public class TestInCodeGenerator
{
    @Test
    public void testDirectSwitchBigint()
    {
        List<RowExpression> values = new ArrayList<>();
        values.add(new ConstantExpression(1L, BIGINT));
        values.add(new ConstantExpression(2L, BIGINT));
        values.add(new ConstantExpression(3L, BIGINT));
        SwitchGenerationCase switchGenerationCase = checkSwitchGenerationCase(BIGINT, values);
        Assert.assertEquals(switchGenerationCase, DIRECT_SWITCH);

        values.add(new ConstantExpression(null, BIGINT));
        switchGenerationCase = checkSwitchGenerationCase(BIGINT, values);
        Assert.assertEquals(switchGenerationCase, DIRECT_SWITCH);
        values.add(new CallExpression(
                new Signature(
                        CAST,
                        SCALAR,
                        BIGINT.getDisplayName(),
                        DOUBLE.getDisplayName()
                ),
                BIGINT,
                Collections.singletonList(new ConstantExpression(5.0D, DOUBLE))
        ));
        switchGenerationCase = checkSwitchGenerationCase(BIGINT, values);
        Assert.assertEquals(switchGenerationCase, DIRECT_SWITCH);

        for  (int i = 6; i <= 1000; ++i) {
            values.add(new ConstantExpression(Long.valueOf(i), BIGINT));
        }
        switchGenerationCase = checkSwitchGenerationCase(BIGINT, values);
        Assert.assertEquals(switchGenerationCase, DIRECT_SWITCH);
    }

    @Test
    public void testDirectSwitchDate()
    {
        List<RowExpression> values = new ArrayList<>();
        values.add(new ConstantExpression(1L, DATE));
        values.add(new ConstantExpression(2L, DATE));
        values.add(new ConstantExpression(3L, DATE));
        SwitchGenerationCase switchGenerationCase = checkSwitchGenerationCase(DATE, values);
        Assert.assertEquals(switchGenerationCase, DIRECT_SWITCH);
    }

    @Test
    public void testHashSwitch()
    {
        List<RowExpression> values = new ArrayList<>();
        values.add(new ConstantExpression(1.5D, DOUBLE));
        values.add(new ConstantExpression(2.5D, DOUBLE));
        values.add(new ConstantExpression(3.5D, DOUBLE));
        SwitchGenerationCase switchGenerationCase = checkSwitchGenerationCase(DOUBLE, values);
        Assert.assertEquals(switchGenerationCase, HASH_SWITCH);

        values.add(new ConstantExpression(null, DOUBLE));
        switchGenerationCase = checkSwitchGenerationCase(DOUBLE, values);
        Assert.assertEquals(switchGenerationCase, HASH_SWITCH);

        for  (int i = 5; i <= 1000; ++i) {
            values.add(new ConstantExpression(Double.valueOf(i + 0.5D), DOUBLE));
        }
        switchGenerationCase = checkSwitchGenerationCase(DOUBLE, values);
        Assert.assertEquals(switchGenerationCase, HASH_SWITCH);
    }

    @Test
    public void testSetContains()
    {
        List<RowExpression> values = new ArrayList<>();
        for  (int i = 1; i <= 1001; ++i) {
            values.add(new ConstantExpression(Long.valueOf(i), BIGINT));
        }
        SwitchGenerationCase switchGenerationCase = checkSwitchGenerationCase(BIGINT, values);
        Assert.assertEquals(switchGenerationCase, SET_CONTAINS);

        for  (int i = 1; i <= 1001; ++i) {
            values.set(i - 1, new ConstantExpression(Double.valueOf(i + 0.5D), DOUBLE));
        }
        switchGenerationCase = checkSwitchGenerationCase(BIGINT, values);
        Assert.assertEquals(switchGenerationCase, SET_CONTAINS);
    }
}
