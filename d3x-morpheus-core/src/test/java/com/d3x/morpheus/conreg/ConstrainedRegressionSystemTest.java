/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.conreg;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.d3x.morpheus.apache.ApacheRealVector;
import com.d3x.morpheus.frame.DataFrame;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConstrainedRegressionSystemTest extends ConstrainedRegressionTestBase {
    private final ConstrainedRegressionModel<String> model;
    private final DataFrame<String, String> frame;
    private final ConstrainedRegressionSystem<String, String> system;

    public ConstrainedRegressionSystemTest() {
        this.model = buildConstrainedModel();
        this.frame = buildObservationFrame();
        this.system = ConstrainedRegressionSystem.build(model, frame);
    }

    @Test
    public void testAugmentedMatrix() {
        RealMatrix actual = system.getAugmentedMatrix();

        assertEquals(actual.getRowDimension(), 9);
        assertEquals(actual.getColumnDimension(), 9);

        // These values have been verified by a separate calculation in R...
        //
        //   20    -6   174    -60     6    11     3     0     0
        //   -6   180   -72   2712   -22     3    13     1     0
        //  174   -72  2676   -564    84    33    57     2     0
        //  -60  2712  -564  51000  -334    21   253     0     0
        //    6   -22    84   -334     6     0     0     0     1
        //   11     3    33     21     0    11     0     0     1
        //    3    13    57    253     0     0     3     0     1
        //    0     1     2      0     0     0     0     0     0
        //    0     0     0      0     1     1     1     0     0
        //
        double tolerance = 1.0E-12;
        assertEquals(actual.getEntry(0, 0),   20.0, tolerance);
        assertEquals(actual.getEntry(0, 2),  174.0, tolerance);
        assertEquals(actual.getEntry(1, 3), 2712.0, tolerance);
        assertEquals(actual.getEntry(1, 7),    1.0, tolerance);
        assertEquals(actual.getEntry(2, 1),  -72.0, tolerance);
        assertEquals(actual.getEntry(2, 7),    2.0, tolerance);
        assertEquals(actual.getEntry(3, 6),  253.0, tolerance);
        assertEquals(actual.getEntry(4, 3), -334.0, tolerance);
        assertEquals(actual.getEntry(4, 8),    1.0, tolerance);
        assertEquals(actual.getEntry(5, 0),   11.0, tolerance);
        assertEquals(actual.getEntry(5, 8),    1.0, tolerance);
        assertEquals(actual.getEntry(6, 6),    3.0, tolerance);
        assertEquals(actual.getEntry(6, 8),    1.0, tolerance);
        assertEquals(actual.getEntry(7, 1),    1.0, tolerance);
        assertEquals(actual.getEntry(7, 2),    2.0, tolerance);
        assertEquals(actual.getEntry(8, 4),    1.0, tolerance);
        assertEquals(actual.getEntry(8, 5),    1.0, tolerance);
        assertEquals(actual.getEntry(8, 6),    1.0, tolerance);
    }

    @Test
    public void testAugmentedVector() {
        RealVector actual = system.getAugmentedVector();
        assertEquals(actual.getDimension(), 9);

        // These values have been verified by a separate calculation in R...
        double tolerance = 0.0001;
        assertEquals(actual.getEntry(0),    610.0861, tolerance);
        assertEquals(actual.getEntry(1),  -2756.0688, tolerance);
        assertEquals(actual.getEntry(2),   7998.5778, tolerance);
        assertEquals(actual.getEntry(3), -50048.5164, tolerance);
        assertEquals(actual.getEntry(4),    558.0238, tolerance);
        assertEquals(actual.getEntry(5),    136.0556, tolerance);
        assertEquals(actual.getEntry(6),    -83.9934, tolerance);
        assertEquals(actual.getEntry(7),      3.0,    tolerance);
        assertEquals(actual.getEntry(8),      0.0,    tolerance);
    }

    @Test
    public void testDesignMatrix() {
        RealMatrix actual = system.getDesignMatrix();

        for (int row = 0; row < actual.getRowDimension(); row++)
            for (int col = 0; col < actual.getColumnDimension(); col++)
                assertEquals(actual.getEntry(row, col), frame.getDouble(observationRows.get(row), getRegressors().get(col)));
    }

    @Test
    public void testRegressandVector() {
        RealVector actual = system.getRegressandVector();

        for (int row = 0; row < actual.getDimension(); row++)
            assertEquals(actual.getEntry(row), frame.getDouble(observationRows.get(row), regressand), 1.0E-12);
    }

    @Test
    public void testWeightVector() {
        // There are ten non-zero weights, so the weights should be rescaled to sum to 10.0...
        assertTrue(comparator.equals(system.getWeightVector(), ApacheRealVector.of(0.5, 1.0, 1.5, 2.0, 0.0, 0.5, 1.0, 1.5, 0.5, 1.0, 0.5)));
    }
}