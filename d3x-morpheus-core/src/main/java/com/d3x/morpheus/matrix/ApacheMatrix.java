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
package com.d3x.morpheus.matrix;

import com.d3x.morpheus.vector.D3xVector;

import lombok.NonNull;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Wraps an Apache RealMatrix implementation in a D3xMatrix interface.
 *
 * @author Scott Shaffer
 */
public final class ApacheMatrix implements D3xMatrix {
    @NonNull private final RealMatrix impl;

    private ApacheMatrix(RealMatrix impl) {
        this.impl = impl;
    }

    /**
     * Returns a read-only RealMatrix view of a generic D3xMatrix to be used
     * as the operand in algebraic functions.  Modifying the returned matrix
     * is not permitted, and the results of doing so are undefined.
     *
     * @param matrix the algebraic operand.
     *
     * @return a read-only RealMatrix view of the specified operand.
     */
    public static RealMatrix asOperand(D3xMatrix matrix) {
        //
        // Finds the best RealMatrix implementation to use when operating on
        // generic D3xMatrix instances.
        //
        if (matrix instanceof ApacheMatrix)
            return ((ApacheMatrix) matrix).impl;

        if (matrix instanceof RealMatrix)
            return (RealMatrix) matrix;

        return new Array2DRowRealMatrix(matrix.toArray(), false);
    }

    /**
     * Creates a new matrix by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new matrix containing a copy of the specified array.
     */
    public static ApacheMatrix copyOf(double[][] values) {
        //
        // The BlockRealMatrix is more cache-friendly for large matrices,
        // and has only minimal overhead for small matrices, so we prefer
        // it to Array2DRowRealMatrix...
        //
        return wrap(new BlockRealMatrix(values));
    }

    /**
     * Creates a new matrix with dense physical storage and all elements
     * initialized to zero.
     *
     * @param nrow the number of matrix rows.
     * @param ncol the number of matrix columns.
     *
     * @return a new matrix with dense physical storage.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    public static ApacheMatrix dense(int nrow, int ncol) {
        //
        // The BlockRealMatrix is more cache-friendly for large matrices,
        // and has only minimal overhead for small matrices, so we prefer
        // it to Array2DRowRealMatrix...
        //
        return wrap(new BlockRealMatrix(nrow, ncol));
    }

    /**
     * Creates a new matrix with storage only for diagonal elements (all
     * diagonal elements initialized to zero).
     *
     * @param N the length of the diagonal.
     *
     * @return a new matrix with storage for only diagonal elements.
     *
     * @throws RuntimeException if the diagonal length is negative.
     */
    public static ApacheMatrix diagonal(int N) {
        return wrap(new DiagonalMatrix(N));
    }

    /**
     * Creates a new matrix with storage only for diagonal elements and
     * initializes those elements.
     *
     * @param diagonals the diagonal values to assign.
     *
     * @return a new matrix with storage for only diagonal elements with
     * those elements assigned by the input vector.
     */
    public static ApacheMatrix diagonal(D3xVector diagonals) {
        return wrap(new DiagonalMatrix(diagonals.toArray()));
    }

    /**
     * Creates a new matrix with sparse physical storage and all elements
     * initialized to zero.
     *
     * @param nrow the number of matrix rows.
     * @param ncol the number of matrix columns.
     *
     * @return a new matrix with sparse physical storage.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    public static ApacheMatrix sparse(int nrow, int ncol) {
        return wrap(new OpenMapRealMatrix(nrow, ncol));
    }

    /**
     * Creates a mutable matrix view over a bare array (a shallow copy).
     * Changes to the returned matrix will be reflected in the input
     * array, and changes to the array will be reflected in the matrix.
     *
     * @param values the values to be wrapped.
     *
     * @return a new mutable matrix using the input array for its
     * physical storage.
     */
    public static ApacheMatrix wrap(double[][] values) {
        return wrap(new Array2DRowRealMatrix(values, false));
    }

    /**
     * Wraps an existing RealMatrix implementation in a D3xMatrix interface.
     *
     * @param matrix the RealMatrix implementation to wrap.
     *
     * @return a new D3xMatrix interface with the specified implementation.
     */
    public static ApacheMatrix wrap(RealMatrix matrix) {
        return new ApacheMatrix(matrix);
    }

    @Override
    public ApacheMatrix copy() {
        return new ApacheMatrix(impl.copy());
    }

    @Override
    public double get(int row, int col) {
        return impl.getEntry(row, col);
    }

    @Override
    public ApacheMatrix like(int nrow, int ncol) {
        return new ApacheMatrix(impl.createMatrix(nrow, ncol));
    }

    @Override
    public int nrow() {
        return impl.getRowDimension();
    }

    @Override
    public int ncol() {
        return impl.getColumnDimension();
    }

    @Override
    public void set(int row, int col, double value) {
        impl.setEntry(row, col, value);
    }

    @Override
    public D3xVector times(D3xVector x) {
        return D3xVector.wrap(impl.operate(x.toArray()));
    }

    @Override
    public ApacheMatrix times(D3xMatrix B) {
        return wrap(this.impl.multiply(asOperand(B)));
    }

    @Override
    public ApacheMatrix transpose() {
        return wrap(impl.transpose());
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}