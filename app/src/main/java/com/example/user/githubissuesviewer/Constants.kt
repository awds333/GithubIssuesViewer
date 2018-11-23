package com.example.user.githubissuesviewer

class Types(){
    companion object {
        const val EXCEPTION : Int = 0;
        const val REPO : Int = 1;
        const val ISSUES : Int = 2;
        const val AVATAR : Int = 3;
    }
}

class Exceptions(){
    companion object {
        const val NETWORK_EXCEPTION : Int = 0
        const val SEARCH_EXCEPTION : Int = 1;
    }
}
