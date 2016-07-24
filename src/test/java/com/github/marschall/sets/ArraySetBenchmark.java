package com.github.marschall.sets;

import java.util.HashSet;
import java.util.Set;
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
public class ArraySetBenchmark {

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
            .include(".*ArraySetBenchmark.*")
            .warmupIterations(10)
            .measurementIterations(10)
            .build();
    new Runner(options).run();
  }

  private static final Integer[] INTEGERS;

  static {
    INTEGERS = new Integer[6];
    for (int i = 0; i < INTEGERS.length; i++) {
      INTEGERS[i] = i;
    }
  }

  private Set<Integer> hashSet3;
  private Set<Integer> hashSet6;

  private Set<Integer> arraySet3;
  private Set<Integer> arraySet6;

  @Setup
  public void setup() {
    this.hashSet3 = new HashSet<>(4);
    this.hashSet6 = new HashSet<>(8);
    this.arraySet3 = new ArraySet<>(3);
    this.arraySet6 = new ArraySet<>(6);

    for (int i = 0; i < 3; i++) {
      this.hashSet3.add(i);
      this.arraySet3.add(i);
    }

    for (int i = 0; i < 6; i++) {
      this.hashSet6.add(i);
      this.arraySet6.add(i);
    }
  }

  @Benchmark
  public boolean hashSet3() {
    for (int i = 0; i < 3; i++) {
      if (!this.hashSet3.contains(INTEGERS[i])) {
        return false;
      }
    }
    return true;
  }

  @Benchmark
  public boolean hashSet6() {
    for (int i = 0; i < 6; i++) {
      if (!this.hashSet6.contains(INTEGERS[i])) {
        return false;
      }
    }
    return true;
  }

  @Benchmark
  public boolean arraySet3() {
    for (int i = 0; i < 3; i++) {
      if (!this.arraySet3.contains(INTEGERS[i])) {
        return false;
      }
    }
    return true;
  }

  @Benchmark
  public boolean arraySet6() {
    for (int i = 0; i < 6; i++) {
      if (!this.arraySet6.contains(INTEGERS[i])) {
        return false;
      }
    }
    return true;
  }

}
