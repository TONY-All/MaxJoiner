package cn.maxmc.maxjoiner.server

data class Server(val bcName: String, val name: String, var canSpectate: Boolean, val url: String, val port: Int, var currentState: ServerInfo, var canJoin: Boolean): Comparable<Server> {
    override fun compareTo(other: Server): Int {
        // if one can't join
        if ((!(this.canJoin && other.canJoin)) && (other.canJoin || this.canJoin)) {
            return if(this.canJoin) 1 else -1
        }

        // if one is offline
        if ((!(this.currentState.isOnline && other.currentState.isOnline)) && (this.currentState.isOnline || other.currentState.isOnline)) {
            return if(this.currentState.isOnline) 1 else 0
        }

        return this.currentState.current - other.currentState.current
    }
}