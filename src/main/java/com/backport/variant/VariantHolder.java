package com.backport.variant;

/** Implemented (via mixin) by every Animal; only pig, cow and chicken actually carry a variant. */
public interface VariantHolder {
   int backport$getVariant();

   void backport$setVariant(int variant);
}
