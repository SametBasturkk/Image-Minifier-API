<?php

if (!defined('ABSPATH')) {
    exit;
}

class ImageCompressionPro {
    private $api_url = 'http://185.136.206.146:8085/api/images/upload';
    private $quality = 70;
    private $api_key = '4b844ad3-e38d-443c-a917-559760f1ca66';
    private $exclude_sizes = array();
    private $token ="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJZV1BUck9iOVRLaC0zN3ZsY1NwbmF6VTFEYUFOc0dfWGJaU3RQQUxSSC1BIn0.eyJleHAiOjE3MjU3MDUyMTAsImlhdCI6MTcyNTcwNDkxMCwianRpIjoiYzE1MTk4NTctZjcyNy00ZTA5LWE0NjQtNTcxZWQyZjgxMGRlIiwiaXNzIjoiaHR0cDovLzE4NS4xMzYuMjA2LjE0Njo4MDgxL2F1dGgvcmVhbG1zL21pbmlmaWVyIiwic3ViIjoiOTA2ZWIzMTYtNGI0Zi00ZjMzLWExYjgtY2QwZTczNzJjMTdmIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYWRtaW4tY2xpIiwic2Vzc2lvbl9zdGF0ZSI6IjI2NmEzZjc2LTdiMDQtNGM4MC1hYjg0LTRiMjhhZTc1OTE0ZCIsImFjciI6IjEiLCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJzaWQiOiIyNjZhM2Y3Ni03YjA0LTRjODAtYWI4NC00YjI4YWU3NTkxNGQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6ImFuZyBhIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiZGVuZW1lMSIsImdpdmVuX25hbWUiOiJhbmciLCJmYW1pbHlfbmFtZSI6ImEiLCJlbWFpbCI6InRlc3RAZ21haWwuY29tIn0.Kr4kUDyWQjlQMjPYMz302pL1DbIsMKJktw2piOYxmTcbRo4DK7YVgmAC21slxZbhwwa37GUIRhWrj1AiyNmU6xUpDwJAt3P-4090hj5ruUUM-_nvBiOnMEC_VlHmico9a4lpJcEwV2er9Ujr4pGFpiJ0VvwFMdA3aqefnOVAMpeq0lRPDigULRnbl-gEcHGEkRREHfIn9KiyxgkkRwe3jrDbF8SPZ9QWdBOGxaWWdJEzVfGAjEMI0Oe4hd_n8_3LJQkRZv2F_ErUVCERBL8hioNowyhvlcTOoZjf3f1lZ9LqDcRRY82hxJcpcU7PkrOn2zqzspK3FVNFOsJWYi_hcg";
    private $automatic_compression = false;
    private $backup_original = false;

    public function __construct() {
        add_action('admin_menu', array($this, 'add_plugin_page'));
        add_action('admin_init', array($this, 'register_settings'));
        add_action('admin_enqueue_scripts', array($this, 'enqueue_admin_scripts'));
        add_filter('wp_handle_upload', array($this, 'compress_uploaded_image'));
        add_action('wp_ajax_compress_existing_image', array($this, 'compress_existing_image'));
        add_action('wp_ajax_get_compression_stats', array($this, 'get_compression_stats'));
        add_action('wp_ajax_get_compression_progress', array($this, 'get_compression_progress'));
        add_action('wp_ajax_restore_original_image', array($this, 'restore_original_image'));
        add_action('admin_notices', array($this, 'show_admin_notices'));
        add_action('wp_ajax_bulk_compress_images', array($this, 'bulk_compress_images'));

        $this->api_key = get_option('image_compression_pro_api_key', '');
        $this->quality = get_option('image_compression_pro_quality', 70);
        $this->exclude_sizes = get_option('image_compression_pro_exclude_sizes', array());
        $this->automatic_compression = get_option('image_compression_pro_automatic_compression', false);
        $this->backup_original = get_option('image_compression_pro_backup_original', false);

        if ($this->automatic_compression) {
            $this->start_background_compression();
        }
    }

    public function add_plugin_page() {
        add_menu_page(
            'Image Compression Pro',
            'Image Compression',
            'manage_options',
            'image-compression-pro',
            array($this, 'create_admin_page'),
            'dashicons-images-alt2',
            60
        );
    }

    public function register_settings() {
        register_setting('image_compression_pro_options', 'image_compression_pro_api_key');
        register_setting('image_compression_pro_options', 'image_compression_pro_quality', array(
            'type' => 'integer',
            'sanitize_callback' => function($value) {
                return max(1, min(100, intval($value)));
            }
        ));
        register_setting('image_compression_pro_options', 'image_compression_pro_exclude_sizes', array(
            'type' => 'array',
            'sanitize_callback' => function($value) {
                if ($value === null) {
                    $value = array();
                }
                return array_map('sanitize_text_field', $value);
            }
        ));
        register_setting('image_compression_pro_options', 'image_compression_pro_automatic_compression', 'boolval');
        register_setting('image_compression_pro_options', 'image_compression_pro_backup_original', 'boolval');
    }

    public function enqueue_admin_scripts($hook) {
        if ($hook != 'toplevel_page_image-compression-pro') {
            return;
        }
        wp_enqueue_style('image-compression-pro-css', plugin_dir_url(__FILE__) . 'image-compression-pro.css');
        wp_enqueue_script('image-compression-pro-js', plugin_dir_url(__FILE__) . 'image-compression-pro.js', array('jquery'), '1.5', true);
        wp_localize_script('image-compression-pro-js', 'image_compression_pro', array(
            'ajax_url' => admin_url('admin-ajax.php'),
            'nonce' => wp_create_nonce('image_compression_pro_nonce')
        ));
    }

    public function create_admin_page() {
        ?>
        <div class="wrap">
            <h1>Image Compression Pro</h1>

            <div class="notice notice-info">
                <p><strong>API Key:</strong> You can obtain an API key from <a href="https://yourwebsite.com/api-keys" target="_blank">yourwebsite.com/api-keys</a></p>
            </div>

            <form method="post" action="options.php">
                <?php
                settings_fields('image_compression_pro_options');
                do_settings_sections('image_compression_pro_options');
                ?>
                <table class="form-table">
                    <tr valign="top">
                        <th scope="row">API Key</th>
                        <td><input type="text" name="image_compression_pro_api_key" value="<?php echo esc_attr($this->api_key); ?>" /></td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Compression Ratio</th>
                        <td>
                            <input type="number" name="image_compression_pro_quality" id="image_compression_pro_quality" min="1" max="100" value="<?php echo esc_attr($this->quality); ?>" />
                            <p class="description">Enter a value between 1 and 100 to adjust compression ratio</span></p>
                        </td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Exclude Sizes</th>
                        <td>
                            <?php
                            $sizes = get_intermediate_image_sizes();
                            foreach ($sizes as $size) {
                                $checked = in_array($size, $this->exclude_sizes) ? 'checked' : '';
                                echo '<input type="checkbox" name="image_compression_pro_exclude_sizes[]" value="' . $size . '" ' . $checked . ' /> ' . $size . '<br>';
                            }
                            ?>
                            <p class="description">Select image sizes to exclude from compression.</p>
                        </td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Automatic Compression</th>
                        <td>
                            <input type="checkbox" name="image_compression_pro_automatic_compression" <?php checked($this->automatic_compression); ?> />
                            <p class="description">Enable automatic compression of newly uploaded images.</p>
                        </td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">Backup Original Images</th>
                        <td>
                            <input type="checkbox" name="image_compression_pro_backup_original" <?php checked($this->backup_original); ?> />
                            <p class="description">Create a backup of original images before compression.</p>
                        </td>
                    </tr>
                </table>
                <?php submit_button(); ?>
            </form>

            <div class="image-compression-pro-container">
                <div class="image-compression-pro-stats">
                    <h2>Compression Statistics</h2>
                    <div id="compression-stats-container">
                        <!-- Stats will be populated via AJAX -->
                    </div>
                </div>

                <div class="image-compression-pro-compress">
                    <h2>Compress Existing Images</h2>
                    <p>Select an image from the dropdown and click "Compress Image".</p>
                    <select id="image-select">
                        <?php
                        $attachments = get_posts(array(
                            'post_type' => 'attachment',
                            'post_mime_type' => 'image',
                            'posts_per_page' => -1,
                        ));
                        foreach ($attachments as $attachment) {
                            $original_url = wp_get_attachment_url($attachment->ID);
                            echo '<option value="' . $attachment->ID . '" data-original-url="' . $original_url . '">' . $attachment->post_title . '</option>';
                        }
                        ?>
                    </select>
                    <button id="compress-image-button" class="button button-primary">Compress Image</button>
                    <div id="compression-progress"></div>
                    <div id="compression-result"></div>

                    <!-- Bulk Compression -->
                    <h2>Bulk Compress Images</h2>
                    <button id="bulk-compress-button" class="button button-primary">Compress All Images</button>
                    <div id="bulk-compression-progress"></div>
                    <div id="bulk-compression-result"></div>

                    <div class="image-compression-pro-restore">
                    <h2>Restore Original Images</h2>
                    <p>Select an image from the dropdown and click "Restore Original".</p>
                    <select id="image-restore-select">
                        <?php
                        $attachments = get_posts(array(
                            'post_type' => 'attachment',
                            'post_mime_type' => 'image',
                            'posts_per_page' => -1,
                        ));
                        foreach ($attachments as $attachment) {
                            $original_url = wp_get_attachment_url($attachment->ID);
                            echo '<option value="' . $attachment->ID . '" data-original-url="' . $original_url . '">' . $attachment->post_title . '</option>';
                        }
                        ?>
                    </select>
                    <button id="restore-image-button" class="button button-primary">Restore Original</button>
                    <div id="restore-result"></div>
                </div>
                </div>
            </div>
        </div>
        <?php
    }

    public function compress_uploaded_image($upload) {
        if (strpos($upload['type'], 'image') === false) {
            return $upload;
        }

        if ($this->automatic_compression) {
            $this->compress_image_async($upload['file']);
        }

        return $upload;
    }

    private function compress_image_async($file_path) {
        $args = array(
            'file_path' => $file_path,
        );
        wp_schedule_single_event(time(), 'image_compression_pro_compress_image', $args);
    }

    public function compress_existing_image() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $attachment_id = intval($_POST['attachment_id']);
        $file_path = get_attached_file($attachment_id);

        $this->update_compression_progress(0, $attachment_id);

        $compressed_image = $this->compress_image($file_path);

        if ($compressed_image) {
            $original_filename = basename($file_path);

            $file_dir = dirname($file_path);

            $new_file_path = path_join($file_dir, $original_filename);

            file_put_contents($new_file_path, base64_decode($compressed_image['compressedImage']));

            $this->update_compression_stats($compressed_image);

            $this->update_compression_progress(100, $attachment_id);

            $this->update_image_metadata($new_file_path, $compressed_image);

            if ($this->backup_original) {
                $backup_path = $this->get_backup_path($attachment_id);
                copy($new_file_path, $backup_path);
            }

            $this->regenerate_thumbnails($attachment_id);

            wp_send_json_success($compressed_image);
        } else {
            wp_send_json_error('Compression failed');
        }
    }

    public function get_compression_stats() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $stats = get_option('image_compression_pro_stats', array(
            'total_compressions' => 0,
            'total_saved_bytes' => 0,
            'average_compression_ratio' => 0,
        ));

        wp_send_json_success($stats);
    }

    public function get_compression_progress() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $attachment_id = intval($_POST['attachment_id']);
        $progress_data = get_option('image_compression_pro_progress', array());
        $progress = isset($progress_data[$attachment_id]) ? $progress_data[$attachment_id] : 0;

        wp_send_json_success($progress);
    }

    private function compress_image($file_path) {
        if (!function_exists('curl_init')) {
            return false;
        }

        $curl = curl_init();

        curl_setopt_array($curl, array(
            CURLOPT_URL => $this->api_url,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_ENCODING => '',
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_TIMEOUT => 0,
            CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => 'POST',
            CURLOPT_POSTFIELDS => array('file'=> new CURLFILE($file_path),'quality' => $this->quality,'api_key' => $this->api_key,'token' => $this->token),
            CURLOPT_HTTPHEADER => array(
                'Content-Type: multipart/form-data',
                'Accept: */*'
            ),
        ));

        $response = curl_exec($curl);
        curl_close($curl);

        return json_decode($response, true);
    }

    private function update_compression_stats($compressed_image) {
        $stats = get_option('image_compression_pro_stats', array(
            'total_compressions' => 0,
            'total_saved_bytes' => 0,
            'average_compression_ratio' => 0,
        ));

        $stats['total_compressions']++;
        $stats['total_saved_bytes'] += ($compressed_image['originalSize'] - $compressed_image['compressedSize']);
        $stats['average_compression_ratio'] = (($stats['average_compression_ratio'] * ($stats['total_compressions'] - 1)) + $compressed_image['compressionRatio']) / $stats['total_compressions'];

        update_option('image_compression_pro_stats', $stats);
    }

    private function update_compression_progress($progress, $attachment_id) {
        $progress_data = get_option('image_compression_pro_progress', array());
        $progress_data[$attachment_id] = $progress;
        update_option('image_compression_pro_progress', $progress_data);
    }

    public function restore_original_image() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $attachment_id = intval($_POST['attachment_id']);
        $backup_path = $this->get_backup_path($attachment_id);
        $original_path = get_attached_file($attachment_id);

        if (file_exists($backup_path)) {
            if (copy($backup_path, $original_path)) {
                wp_send_json_success('Original image restored.');
            } else {
                wp_send_json_error('Failed to restore original image.');
            }
        } else {
            wp_send_json_error('Backup file not found.');
        }
    }

    private function get_backup_path($attachment_id) {
        $file_path = get_attached_file($attachment_id);
        return $file_path . '.bak';
    }

    private function start_background_compression() {
        add_action('image_compression_pro_compress_image', array($this, 'background_compress_image'));
    }

    public function background_compress_image($args) {
        $file_path = $args['file_path'];

        $image_metadata = wp_get_attachment_metadata(attachment_url_to_postid($file_path));
        if (!$image_metadata || empty($image_metadata['file'])) {
            return;
        }

        $file_size = filesize($file_path);
        if ($this->should_exclude_image($file_size, $image_metadata)) {
            return;
        }

        $compressed_image = $this->compress_image($file_path);

        if ($compressed_image) {
            $this->update_image_metadata($file_path, $compressed_image);

            if ($this->backup_original) {
                $backup_path = $this->get_backup_path(attachment_url_to_postid($file_path));
                copy($file_path, $backup_path);
            }

            $this->regenerate_thumbnails(attachment_url_to_postid($file_path));
        }
    }

    private function should_exclude_image($file_size, $image_metadata) {
        $exclude_size_limit = get_option('image_compression_pro_exclude_size_limit', 0);
        if ($file_size < $exclude_size_limit) {
            return true;
        }

        foreach ($this->exclude_sizes as $size) {
            if (isset($image_metadata['sizes'][$size])) {
                return true;
            }
        }

        return false;
    }

    private function update_image_metadata($file_path, $compressed_image) {
        $attachment_id = attachment_url_to_postid($file_path);
        if (!$attachment_id) {
            error_log('Could not retrieve attachment ID for file: ' . $file_path);
            return;
        }

        $image_metadata = wp_get_attachment_metadata($attachment_id);
        if (!$image_metadata) {
            error_log('Failed to retrieve metadata for attachment ID ' . $attachment_id);
            return;
        }

        $image_metadata['filesize'] = $compressed_image['compressedSize'];

        if (isset($image_metadata['sizes'])) {
            foreach ($image_metadata['sizes'] as $size => $data) {
                $size_file_path = path_join(dirname($file_path), $data['file']);
                if (file_exists($size_file_path)) {
                    $image_metadata['sizes'][$size]['filesize'] = filesize($size_file_path);
                } else {
                    error_log('File does not exist for size "' . $size . '": ' . $size_file_path);
                }
            }
        }

        $update_metadata_result = wp_update_attachment_metadata($attachment_id, $image_metadata);
        if (is_wp_error($update_metadata_result)) {
            error_log('Failed to update attachment metadata for attachment ID ' . $attachment_id);
            return;
        }

        clean_attachment_cache($attachment_id);

        $update_post_result = wp_update_post(array(
            'ID' => $attachment_id,
            'post_modified' => current_time('mysql'),
            'post_modified_gmt' => current_time('mysql', 1)
        ));

        if (is_wp_error($update_post_result)) {
            error_log('Failed to update post for attachment ID ' . $attachment_id);
            return;
        }

        clean_post_cache($attachment_id);

        wp_cache_flush();

        error_log('Successfully updated metadata and post for attachment ID ' . $attachment_id);
    }

    public function show_admin_notices() {
        if (isset($_GET['image_compression_pro_notice'])) {
            $notice = sanitize_text_field($_GET['image_compression_pro_notice']);
            echo '<div class="notice notice-success is-dismissible"><p>' . $notice . '</p></div>';
        }
    }

    private function regenerate_thumbnails($attachment_id) {
        wp_update_attachment_metadata($attachment_id, wp_generate_attachment_metadata($attachment_id, get_attached_file($attachment_id)));
    }

    public function bulk_compress_images() {
        check_ajax_referer('image_compression_pro_nonce', 'nonce');

        if (!current_user_can('manage_options')) {
            wp_die('Unauthorized');
        }

        $attachments = get_posts(array(
            'post_type' => 'attachment',
            'post_mime_type' => 'image',
            'posts_per_page' => -1,
        ));

        $totalImages = count($attachments);
        $compressedImages = 0;

        $this->update_bulk_compression_progress(0, $totalImages);

        foreach ($attachments as $attachment) {
            $file_path = get_attached_file($attachment->ID);

            $this->update_compression_progress(0, $attachment->ID);

            $compressed_image = $this->compress_image($file_path);

            if ($compressed_image) {
                file_put_contents($file_path, base64_decode($compressed_image['compressedImage']));
                $this->update_compression_stats($compressed_image);

                $this->update_compression_progress(100, $attachment->ID);

                $this->update_image_metadata($file_path, $compressed_image);

                if ($this->backup_original) {
                    $backup_path = $this->get_backup_path($attachment->ID);
                    copy($file_path, $backup_path);
                }

                $this->regenerate_thumbnails($attachment->ID);

                $compressedImages++;
            }

            $this->update_bulk_compression_progress($compressedImages, $totalImages);
        }

        $this->update_bulk_compression_progress($totalImages, $totalImages);
        $this->update_compression_stats($compressed_image);

        wp_send_json_success(array(
            'message' => sprintf('Compressed %d out of %d images.', $compressedImages, $totalImages),
            'stats' => get_option('image_compression_pro_stats')
        ));
    }

    private function update_bulk_compression_progress($completed, $total) {
        $progress = round(($completed / $total) * 100);
        update_option('image_compression_pro_bulk_progress', $progress);
    }
}
$image_compression_pro = new ImageCompressionPro();

function image_compression_pro_css() {
    ?>
    <style>
        .image-compression-pro-container {
            display: flex;
            justify-content: space-between;
            margin-top: 20px;
        }
        .image-compression-pro-stats,
        .image-compression-pro-compress {
            width: 48%;
            background: #fff;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        #compression-stats-container,
        #compression-result,
        #bulk-compression-progress,
        #bulk-compression-result {
            margin-top: 20px;
        }
        #image-select {
            width: 100%;
            margin-bottom: 10px;
        }
        #compress-image-button,
        #bulk-compress-button {
            width: 100%;
        }
        #compression-progress,
        #bulk-compression-progress {
            margin-top: 10px;
        }
    </style>
    <?php
}
add_action('admin_head', 'image_compression_pro_css');

function image_compression_pro_js() {
    ?>
    <script>
        jQuery(document).ready(function($) {
            $('#image_compression_pro_quality').on('input', function() {
                $('#quality-value').text($(this).val());
            });

            $('#restore-image-button').on('click', function() {
            var attachmentId = $('#image-restore-select').val();

            $.ajax({
                url: image_compression_pro.ajax_url,
                type: 'POST',
                data: {
                    action: 'restore_original_image',
                    nonce: image_compression_pro.nonce,
                    attachment_id: attachmentId
                },
                success: function(response) {
                    if (response.success) {
                        $('#restore-result').html('<p>' + response.data + '</p>');
                    } else {
                        $('#restore-result').html('<p>' + response.data + '</p>');
                    }
                },
                error: function() {
                    $('#restore-result').html('<p>An error occurred. Please try again later.</p>');
                }
            });
        });

            function loadCompressionStats() {
                $.ajax({
                    url: image_compression_pro.ajax_url,
                    type: 'POST',
                    data: {
                        action: 'get_compression_stats',
                        nonce: image_compression_pro.nonce
                    },
                    success: function(response) {
                        if (response.success) {
                            var stats = response.data;
                            var statsHtml = '<p>Total Compressions: ' + stats.total_compressions + '</p>' +
                                            '<p>Total Saved: ' + formatBytes(stats.total_saved_bytes) + '</p>' +
                                            '<p>Average Compression Ratio: ' + stats.average_compression_ratio.toFixed(2) + '%</p>';
                            $('#compression-stats-container').html(statsHtml);
                        }
                    }
                });
            }

            function formatBytes(bytes, decimals = 2) {
                if (bytes === 0) return '0 Bytes';
                const k = 1024;
                const dm = decimals < 0 ? 0 : decimals;
                const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
                const i = Math.floor(Math.log(bytes) / Math.log(k));
                return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
            }

            $('#compress-image-button').on('click', function() {
                var attachmentId = $('#image-select').val();

                $.ajax({
                    url: image_compression_pro.ajax_url,
                    type: 'POST',
                    data: {
                        action: 'compress_existing_image',
                        nonce: image_compression_pro.nonce,
                        attachment_id: attachmentId
                    },
                    beforeSend: function() {
                        $('#compression-result').html('<p>Compressing... Please wait.</p>');
                        $('#compression-progress').html('<progress value="0" max="100"></progress>');
                    },
                    success: function(response) {
                        if (response.success) {
                            var result = response.data;
                            var resultHtml = '<p>Original Size: ' + formatBytes(result.originalSize) + '</p>' +
                                             '<p>Compressed Size: ' + formatBytes(result.compressedSize) + '</p>' +
                                             '<p>Compression Ratio: ' + result.compressionRatio.toFixed(2) + '%</p>';
                            $('#compression-result').html(resultHtml);
                            loadCompressionStats();
                        } else {
                            $('#compression-result').html('<p>Compression failed. Please check your API key and try again.</p>');
                        }
                    },
                    error: function() {
                        $('#compression-result').html('<p>An error occurred. Please try again later.</p>');
                    },
                    complete: function() {
                        // Hide the progress bar after completion (success or error)
                        $('#compression-progress').html('');
                    }
                });
            });

            // Bulk Compression
            $('#bulk-compress-button').on('click', function() {
                // Show a progress bar and message
                $('#bulk-compression-result').html('<p>Compressing all images... Please wait.</p>');
                $('#bulk-compression-progress').html('<progress value="0" max="100"></progress>');

                // Start the interval ONLY when bulk compression is initiated
                var bulkProgressInterval = setInterval(updateBulkProgress, 1000);

                $.ajax({
                    url: image_compression_pro.ajax_url,
                    type: 'POST',
                    data: {
                        action: 'bulk_compress_images',
                        nonce: image_compression_pro.nonce
                    },
                    success: function(response) {
                        if (response.success) {
                            $('#bulk-compression-result').html('<p>' + response.data.message + '</p>');
                            loadCompressionStats(); // Update stats after bulk compression
                        } else {
                            $('#bulk-compression-result').html('<p>Bulk compression failed. Please try again later.</p>');
                        }
                    },
                    error: function() {
                        $('#bulk-compression-result').html('<p>An error occurred. Please try again later.</p>');
                    },
                    complete: function() {
                        // Clear the interval when bulk compression is complete
                        clearInterval(bulkProgressInterval);
                        $('#bulk-compression-progress').html('');
                    }
                });
            });


            // Update Bulk Progress (this function remains the same)
            function updateBulkProgress() {
                $.ajax({
                    url: image_compression_pro.ajax_url,
                    type: 'POST',
                    data: {
                        action: 'get_compression_progress',
                        nonce: image_compression_pro.nonce
                    },
                    success: function(response) {
                        if (response.success) {
                            $('#bulk-compression-progress progress').val(response.data);
                        }
                    }
                });
            }


            loadCompressionStats();
        });
    </script>
    <?php
}
add_action('admin_footer', 'image_compression_pro_js');

// Add background process function
add_action('image_compression_pro_compress_image', array($image_compression_pro, 'background_compress_image'));