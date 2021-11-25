package io.github.oybek.gdetram

import io.github.oybek.gdetram.model.{Button, Platform}

package object service {
  type Reply = (String, List[List[Button]])
  type UserId = (Platform, Long)
}
