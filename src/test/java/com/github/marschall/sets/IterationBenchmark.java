package com.github.marschall.sets;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class IterationBenchmark {

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
            .include(".*IterationBenchmark.*")
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(5)
            .build();
    new Runner(options).run();
  }

  private long allSet;
  private long lowSet;
  private long highSet;
  private long low4;
  private long high4;


  @Setup
  public void setup() {
    this.allSet = -1L;
    this.lowSet = 1L;
    this.highSet = 1L << 63;
    this.low4 = 0b1111L;
    this.high4 = 0b1111L << 60;
  }

  @Benchmark
  public int iterateAllSize_allSet() {
    return this.iterateAllSize(this.allSet, 64);
  }

  @Benchmark
  public int iterateAllSize_lowSet() {
    return this.iterateAllSize(this.lowSet, 1);
  }

  @Benchmark
  public int iterateAllSize_low4() {
    return this.iterateAllSize(this.low4, 4);
  }

  @Benchmark
  public int iterateAllSize_high4() {
    return this.iterateAllSize(this.high4, 4);
  }

  @Benchmark
  public int iterateAllSize_highSet() {
    return this.iterateAllSize(this.highSet, 1);
  }

//  @Benchmark
//  public int iterateAll_allSet() {
//    return this.iterateAll(this.allSet);
//  }
//
//  @Benchmark
//  public int iterateAll_lowSet() {
//    return this.iterateAll(this.lowSet);
//  }
//
//  @Benchmark
//  public int iterateAll_low4() {
//    return this.iterateAll(this.low4);
//  }
//
//  @Benchmark
//  public int iterateAll_high4() {
//    return this.iterateAll(this.high4);
//  }
//
//  @Benchmark
//  public int iterateAll_highSet() {
//    return this.iterateAll(this.highSet);
//  }

  @Benchmark
  public int iterateDirect_allSet() {
    return this.iterateDirect(this.allSet);
  }

  @Benchmark
  public int iterateDirect_lowSet() {
    return this.iterateDirect(this.lowSet);
  }

  @Benchmark
  public int iterateDirect_low4() {
    return this.iterateDirect(this.low4);
  }

  @Benchmark
  public int iterateDirect_high4() {
    return this.iterateDirect(this.high4);
  }

  @Benchmark
  public int iterateDirect_highSet() {
    return this.iterateDirect(this.highSet);
  }


  int iterateDirect(long bits) {
    int count = 0;
    int i = SmallIntegerSet.log2(Long.lowestOneBit(bits));
    while (true) {
      if (isSetNoCheck(bits, i)) {
        count += 1;
      }
      // 001100
      // 111000
      // 000111
      long lowestOneBit = Long.lowestOneBit(bits & ~(1L << i | ((1L << i) - 1L)));
      if (lowestOneBit == 0L) {
        break;
      }
      i = SmallIntegerSet.log2(lowestOneBit);
    }
    return count;
  }

//  public static void main(String[] args) {
//    IterationBenchmark b = new IterationBenchmark();
//    b.setup();
//    System.out.println(b.iterateAllDirect(b.allSet));
//    System.out.println(b.iterateAllDirect(b.lowSet));
//    System.out.println(b.iterateAllDirect(b.low4));
//    System.out.println(b.iterateAllDirect(b.highSet));
//    System.out.println(b.iterateAllDirect(b.high4));
//  }

  private int iterateAllSize(long bits, int size) {
    int count = 0;
    for (int i = SmallIntegerSet.MIN_VALUE; i <= SmallIntegerSet.MAX_VALUE && size > 0; ++i) {
      if (isSetNoCheck(bits, i)) {
        count += 1;
        size -= 1;
      }
    }
    return count;
  }

  private int iterateAll(long bits) {
    int count = 0;
    for (int i = SmallIntegerSet.MIN_VALUE; i <= SmallIntegerSet.MAX_VALUE; ++i) {
      if (isSetNoCheck(bits, i)) {
        count += 1;
      }
    }
    return count;
  }

  private static boolean isSetNoCheck(long bits, int i) {
    return (bits & (1L << i)) != 0;
  }

}
