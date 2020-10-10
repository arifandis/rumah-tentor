package com.cahstudio.rumahtentor.model.response

import com.cahstudio.rumahtentor.model.Result

data class Response(
    var multicast_id: Long = 0,
    var success: Int = 0, var failure: Int = 0, var canonical_ids: Int = 0,
    var results: List<Result?>? = null
)