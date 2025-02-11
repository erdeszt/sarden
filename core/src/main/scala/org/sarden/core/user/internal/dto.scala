package org.sarden.core.user.internal

import doobie.Read

case class UserDTO(
    id: String,
    name: String,
    password: String,
) derives Read
