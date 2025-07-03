package org.sarden.domain.user.internal

import doobie.Read

case class UserDTO(
    id: String,
    name: String,
    password: String,
) derives Read
