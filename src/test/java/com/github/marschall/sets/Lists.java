package com.github.marschall.sets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class Lists {

  static <T> List<T> toList(Iterator<T> iterator) {
    List<T> result = new ArrayList<>();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    return result;
  }

}
