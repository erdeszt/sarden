package org.sarden.core.domain.sowlog.internal

import zio.*

private[sowlog] trait SowlogRepo

class LiveSowlogRepo() extends SowlogRepo
