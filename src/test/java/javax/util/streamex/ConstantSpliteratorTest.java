/*
 * Copyright 2015 Tagir Valeev
 * 
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
package javax.util.streamex;

import static javax.util.streamex.TestHelpers.*;

import java.util.Collections;
import org.junit.Test;

/**
 * @author Tagir Valeev
 */
public class ConstantSpliteratorTest {
    @Test
    public void testConstant() {
        checkSpliterator("ref", Collections.nCopies(100, "val"), () -> new ConstantSpliterator.ConstRef<>("val", 100));
        checkSpliterator("ref", Collections.nCopies(100, Integer.MIN_VALUE), () -> new ConstantSpliterator.ConstInt(
                Integer.MIN_VALUE, 100));
        checkSpliterator("ref", Collections.nCopies(100, Long.MIN_VALUE), () -> new ConstantSpliterator.ConstLong(
                Long.MIN_VALUE, 100));
        checkSpliterator("ref", Collections.nCopies(100, Double.MIN_VALUE), () -> new ConstantSpliterator.ConstDouble(
                Double.MIN_VALUE, 100));
    }
}
