/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.docs.basic;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.d3x.morpheus.docs.DemoData;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.Collect;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Tuple;
import com.d3x.morpheus.util.text.printer.Printer;

public class FindingDocs {

    @Test()
    public void findFirstRow() {
        var frame = DemoData.loadPopulationDatasetWeights();

        frame.out().print(formats -> {
            formats.setPrinter("All Males", Printer.ofDouble("0;-0"));
            formats.setPrinter("All Persons", Printer.ofDouble("0;-0"));
            formats.setPrinter("All Females", Printer.ofDouble("0;-0"));
            formats.setPrinter(Double.class, Printer.ofDouble("0.00'%';-0.00'%'", 100));
        });

        frame.rows().last(row -> {
            var total = row.getDouble("All Persons");
            var maleWeight = row.getDouble("All Males") / total;
            var femaleWeight = row.getDouble("All Females") / total;
            return Math.abs(maleWeight - femaleWeight) > 0.1;
        }).ifPresent(row -> {
            var year = row.key().item(0);
            var borough = row.key().item(1);
            var total = row.getDouble("All Persons");
            var males = (row.getDouble("All Males") / total) * 100d;
            var females = (row.getDouble("All Females") / total) * 100d;
            IO.printf("Male weight: %.2f%%, Female weight: %.2f%% for %s in %s", males, females, year, borough);
        });
    }

    @Test()
    public void findFirstValueInRow() {
        var frame = DemoData.loadPopulationDatasetWeights();
        frame.rows().filter(r -> r.getValue("Borough").equals("Kensington and Chelsea")).forEach(row -> {
            row.first(v -> v.colKey().matches("[MF]\\s+\\d+") && v.getDouble() > 0.01).ifPresent(v -> {
                Tuple rowKey = v.rowKey();
                String group = v.colKey();
                double weight = v.getDouble() * 100d;
                IO.printf("Age group %s has a population of %.2f%% for %s\n", group, weight, rowKey);
            });
        });
    }


    @Test()
    public void findMaxValue() {
        var frame = DemoData.loadPopulationDatasetWeights();
        frame.max(v ->
            v.row().getValue("Borough").equals("Islington") &&
            v.colKey().matches("[MF]\\s+\\d+") &&
            v.getDouble() > 0
        ).ifPresent(max -> {
            var year = max.rowKey().item(0);
            var group = max.colKey();
            var weight = max.getDouble() * 100;
            var borough = max.row().getValue("Borough");
            System.out.printf("Max population is %.2f%% for age group %s in %s, %s", weight, group, borough, year);
        });
    }


    @Test()
    public void findMaxRowValue() {
        var rowKey = Tuple.of(2000, "Islington");
        var frame = DemoData.loadPopulationDatasetWeights();
        frame.row(rowKey).max(v -> v.colKey().matches("[MF]\\s+\\d+") && v.getDouble() > 0).ifPresent(max -> {
            var group = max.colKey();
            var year = max.rowKey().item(0);
            var borough = max.rowKey().item(1);
            var weight = max.getDouble() * 100d;
            System.out.printf("Max population weight for %s in %s is %.2f%% for %s", borough, year, weight, group);
        });
    }


    @Test()
    public void findMaxColumnValue() {
        var boroughs = Collect.asSet("Islington", "Wandsworth", "Kensington and Chelsea");
        var frame = DemoData.loadPopulationDatasetWeights();
        frame.col("F 30").max(v -> boroughs.contains((String)v.row().getValue("Borough"))).ifPresent(max -> {
            var year = max.rowKey().item(0);
            var weight = max.getDouble() * 100d;
            var borough = max.row().getValue("Borough");
            System.out.printf("Max female population weight aged 30 is %.2f%% in %s, %s", weight, borough, year);
        });
    }




    @Test()
    public void binarySearchInColumn() {
        var expected = Tuple.of(2000, "Islington");
        var frame = DemoData.loadPopulationDatasetWeights();
        var weight = frame.getDouble(expected, "F 30");
        frame.rows().sort(true, "F 30"); //Ensure data is sorted
        frame.col("F 30").binarySearch(weight).ifPresent(value -> {
            assert(value.rowKey().equals(expected));
            assert(value.getDouble() == weight);
        });
    }


    @Test()
    public void argmin() {



    }

    @Test()
    public void findMaxRow() {
        var frame = DemoData.loadPopulationDatasetWeights();
        frame.rows().min((row1, row2) -> {
            var ratio1 = row1.getDouble("F 0") / row1.getDouble("M 0");
            var ratio2 = row2.getDouble("F 0") / row2.getDouble("M 0");
            return Double.compare(ratio1, ratio2);
        }).ifPresent(row -> {
            var rowKey = row.key();
            var males = row.getDouble("M 0") * 100d;
            var females = row.getDouble("F 0") * 100d;
            IO.printf("Smallest female / male births = %.2f%%/%.2f%% for %s", females, males, rowKey);
        });
    }


    @Test()
    public void previousRow() {
        var start = LocalDate.of(2014, 1, 1);
        var columns = Range.of(0, 10).map(i -> "Column-" + i);
        var monthEnds = Range.of(0, 12).map(i -> start.plusMonths(i+1).minusDays(1));
        var frame = DataFrame.ofDoubles(monthEnds, columns, v -> Math.random() * 100d);
        frame.out().print(100, formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });

        //Iterate over first 35 days of row axis at daily frequency
        var dates = Range.of(monthEnds.start(), monthEnds.start().plusDays(35), Period.ofDays(1));
        dates.forEach(date -> {
            if (frame.rows().contains(date)) {
                IO.println("Exact match for: " + date);
            } else {
                var lowerKey = frame.rows().lowerKey(date);
                assert(lowerKey.isPresent());
                assert(lowerKey.get().equals(date.withDayOfMonth(1).minusDays(1)));
                IO.printf("Lower match for %s is %s%n", date, lowerKey.get());
            }
        });
    }

}
