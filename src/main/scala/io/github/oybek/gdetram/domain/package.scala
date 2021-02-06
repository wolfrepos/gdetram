package io.github.oybek.gdetram.domain

import io.github.oybek.gdetram.domain.model.{Button, Platform}

package object chain {
  type Reply = (String, List[List[Button]])
  type UserId = (Platform, Long)
}
