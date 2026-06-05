# Fix shadow-native configure failure on newer host distributions:
#   configure: error: crypt() not found
#
# Do not rely on the host libcrypt/libxcrypt development package.
DEPENDS:append:class-native = " libxcrypt-native"
