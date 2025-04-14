/*
 * Copyright 2025 (C) Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.conventions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.security.MessageDigest

/**
 * Object containing data classes and utilities for interacting with the GitLab API.
 *
 * This object provides serializable data classes that map to GitLab API responses
 * and utilities for working with package file hashes.
 */
object GitLabAPI {
    /**
     * Data class representing basic information about a GitLab project.
     *
     * This class is used to deserialize project information from the GitLab API.
     * See [the GitLab API docs](https://docs.gitlab.com/api/projects/)
     *
     * @property id The numeric ID of the GitLab project
     */
    @Serializable
    data class ProjectInfo(
        val id: Long
    )

    /**
     * Data class representing a package in GitLab's package registry.
     *
     * This class is used to deserialize package information from the GitLab API.
     * See [the GitLab API docs](https://docs.gitlab.com/api/packages/)
     *
     * @property id The numeric ID of the package
     * @property name The name of the package
     * @property version The version string of the package
     * @property packageType The type of package (e.g., maven, npm, etc.)
     * @property status The current status of the package
     */
    @Serializable
    data class Package(
        /**
         * The numeric ID of the package.
         */
        val id: Long,

        /**
         * The name of the package.
         */
        val name: String,

        /**
         * The version string of the package.
         */
        val version: String,

        /**
         * The type of package (e.g., maven, npm, etc.).
         */
        @SerialName("package_type") val packageType: String,

        /**
         * The current status of the package.
         */
        val status: String
    )

    /**
     * Enum representing the supported hash types for package files in GitLab.
     *
     * Each enum value provides a way to create a MessageDigest instance for the corresponding
     * hash algorithm.
     *
     * @property digestProvider A function that creates a new MessageDigest instance for the hash algorithm
     */
    enum class PackageFileHashType(private val digestProvider: () -> MessageDigest) {
        // @formatter:off
        MD5     ({ MessageDigest.getInstance("MD5") }),
        SHA1    ({ MessageDigest.getInstance("SHA-1") }),
        SHA256  ({ MessageDigest.getInstance("SHA-256") });
        // @formatter:on

        /**
         * Creates a new MessageDigest instance for this hash type.
         *
         * @return A new MessageDigest instance configured for the hash algorithm
         *         corresponding to this enum value
         */
        fun createDigest(): MessageDigest = digestProvider()
    }

    /**
     * Data class representing a file within a GitLab package.
     *
     * This class is used to deserialize package file information from the GitLab API.
     * See [the GitLab API docs](https://docs.gitlab.com/api/packages/)
     *
     * @property id The numeric ID of the package file
     * @property packageId The ID of the parent package this file belongs to
     * @property fileName The name of the file
     * @property size The size of the file in bytes
     * @property fileMd5 The MD5 hash of the file content if present
     * @property fileSha1 The SHA1 hash of the file content if present
     * @property fileSha256 The SHA256 hash of the file content if present
     */
    @Serializable
    data class PackageFile(
        /**
         * The numeric ID of the package file.
         */
        val id: Long,

        /**
         * The ID of the parent package this file belongs to.
         */
        @SerialName("package_id") val packageId: Long,

        /**
         * The name of the file.
         */
        @SerialName("file_name") val fileName: String,

        /**
         * The size of the file in bytes.
         */
        val size: Long,

        /**
         * The MD5 hash of the file content if present.
         */
        @SerialName("file_md5") val fileMd5: String?,

        /**
         * The SHA1 hash of the file content if present.
         */
        @SerialName("file_sha1") val fileSha1: String?,

        /**
         * The SHA256 hash of the file content if present.
         */
        @SerialName("file_sha256") val fileSha256: String?
    ) {
        /**
         * Determines the hash type used for this package file based on which hash property is non-null.
         *
         * This property is not serialized when the object is serialized.
         *
         * @return The hash type used for this package file, or null if no hash is available
         */
        @Transient
        val hashType: PackageFileHashType? = when {
            fileSha256 != null -> PackageFileHashType.SHA256
            fileSha1 != null -> PackageFileHashType.SHA1
            fileMd5 != null -> PackageFileHashType.MD5
            else -> null
        }

        /**
         * Indicates whether this package file has a hash value.
         *
         * @return true if this package file has a hash value, false otherwise
         */
        inline val hasHash: Boolean
            get() = hashType != null
    }
}
