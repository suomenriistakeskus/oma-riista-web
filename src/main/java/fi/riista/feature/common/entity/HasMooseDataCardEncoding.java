package fi.riista.feature.common.entity;

import fi.riista.util.F;

import javaslang.control.Either;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public interface HasMooseDataCardEncoding<E extends Enum<E>> {

    @Nonnull
    static <E extends Enum<E> & HasMooseDataCardEncoding<E>> Either<Optional<String>, E> enumOf(
            @Nonnull final Class<E> enumClass, @Nullable final String value) {

        return F.trimToOptional(value)
                .map(String::toUpperCase)
                .map(val -> {
                    return Stream.of(enumClass.getEnumConstants())
                            .filter(enumValue -> {
                                return Optional.ofNullable(enumValue.getMooseDataCardEncoding())
                                        .map(String::toUpperCase)
                                        .map(val::equals)
                                        .orElse(false);
                            })
                            .findFirst()
                            .<Either<Optional<String>, E>> map(Either::right)
                            .orElseGet(() -> Either.left(Optional.of(value.trim())));
                })
                .orElseGet(() -> Either.left(Optional.empty()));
    }

    @Nullable
    String getMooseDataCardEncoding();

    default boolean equalsMooseDataCardEncoding(@Nullable final String value) {
        return Optional.ofNullable(getMooseDataCardEncoding())
                .map(String::toUpperCase)
                .flatMap(encoding -> F.trimToOptional(value).map(String::toUpperCase).map(encoding::equals))
                .orElse(false);
    }

}
